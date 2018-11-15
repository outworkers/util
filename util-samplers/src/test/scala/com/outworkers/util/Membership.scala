package com.outworkers.util

trait RoleType extends Enumeration {
  //represents built-in role types.
  type RoleType = Value

  val Leader = Value("leader")
  val AllianceMember = Value("member")
}

object RoleType extends RoleType

case class Membership(
  memberId: String,
  entityType: String,
  allianceId: String,
  role: RoleType.Value = RoleType.Leader,
  rankId: String
)