package com.outworkers.util.samplers

trait FillOptions {
  def apply[T : Sample](opt: Option[T]): Option[T]
}

object Options {
  implicit val alwaysFillOptions: FillOptions = new FillOptions {
    override def apply[T: Sample](opt: Option[T]): Option[T] = {
      opt.orElse(Some(gen[T]))
    }
  }
  implicit val neverFillOptions: FillOptions = new FillOptions {
    override def apply[T: Sample](opt: Option[T]): Option[T] = {
      Option.empty[T]
    }
  }
}
