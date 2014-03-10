package com.newzly.util.core

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

object PasswordHash {
  final val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1"

  // The following constants may be changed without breaking existing hashes.
  final val SALT_BYTE_SIZE = 24
  final val HASH_BYTE_SIZE = 24
  final val PBKDF2_ITERATIONS = 1000

  final val ITERATION_INDEX = 0
  final val SALT_INDEX = 1
  final val PBKDF2_INDEX = 2

  /**
   * Returns a salted PBKDF2 hash of the password.
   * @param   password    the password to hash
   * @return              a salted PBKDF2 hash of the password
   */
  @throws[NoSuchAlgorithmException]
  def createHash(password: String): String = createHash(password.toCharArray)

  /**
   * Returns a salted PBKDF2 hash of the password.
   * @param password the password to hash
   * @return a salted PBKDF2 hash of the password
   */
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def createHash(password: Array[Char]): String = {
    // Generate a random salt
    val random = new SecureRandom()
    val salt = new Array[Byte](SALT_BYTE_SIZE)
    random.nextBytes(salt)

    // Hash the password
    val hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)

    // format iterations:salt:hash
    PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" +  toHex(hash)
  }

  /**
   * Validates a password using a hash.
   * @param password The password to check.
   * @param correctHash The hash of the valid password
   * @return true if the password is correct, false if not
   */
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def validatePassword(password: String, correctHash: String): Boolean = {
    validatePassword(password.toCharArray, correctHash)
  }

  /**
   * Validates a password using a hash.
   * @param password The password to check.
   * @param correctHash The hash of the valid password
   * @return true if the password is correct, false if not
   */
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def validatePassword(password: Array[Char], correctHash: String): Boolean = {
    // Decode the hash into its parameters
    val params = correctHash.split(":")
    val iterations = Integer.parseInt(params(ITERATION_INDEX))
    val salt = fromHex(params(SALT_INDEX))
    val hash = fromHex(params(PBKDF2_INDEX))
    // Compute the hash of the provided password, using the same salt,
    // iteration count, and hash length
    val testHash = pbkdf2(password, salt, iterations, hash.length)
    // Compare the hashes in constant time. The password is correct if
    // both hashes match.
    slowEquals(hash, testHash)
  }

  /**
   * Compares two byte arrays in length-constant time. This comparison method
   * is used so that password hashes cannot be extracted from an on-line 
   * system using a timing attack and then attacked off-line.
   *
   * @param a The first byte array
   * @param b The second byte array
   * @return True if both byte arrays are the same, false if not
   */
  private[this] def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = {
    var diff = a.length ^ b.length
    var i = 0

    while (i < a.length && i < b.length) {
      diff |= a(i) ^ b(i)
      i = i + 1
    }
    diff == 0
  }

  /**
   * Computes the PBKDF2 hash of a password.
   * @param password The password to hash.
   * @param salt The salt.
   * @param iterations The iteration count (slowness factor)
   * @param bytes The length of the hash to compute in bytes
   * @return The PBDKF2 hash of the password.
   */
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  private[this] def pbkdf2(password: Array[Char], salt: Array[Byte], iterations: Int, bytes: Int): Array[Byte] = {
    val spec = new PBEKeySpec(password, salt, iterations, bytes * 8)
    val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
    skf.generateSecret(spec).getEncoded
  }

  /**
   * Converts a string of hexadecimal characters into a byte array.
   * @param hex The hex string
   * @return The hex string decoded into a byte array
   */
  private[this] def fromHex(hex: String): Array[Byte] = {
    val binary = new Array[Byte](hex.length / 2)
    var i = 0
    while (i < binary.length) {
      binary(i) = Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16).asInstanceOf[Byte]
      i = i + 1
    }
    binary
  }

  /**
   * Converts a byte array into a hexadecimal string.
   * @param array The byte array to convert
   * @return A length*2 character string encoding the byte array
   */
  private[this] def toHex(array: Array[Byte]): String = {
    val bi = BigInt(1, array)
    val hex = bi.toString(16)
    val paddingLength = (array.length * 2) - hex.length
    if (paddingLength > 0)
      String.format("%0" + paddingLength + "d", 0.asInstanceOf[AnyRef]) + hex
    else
      hex
  }
}