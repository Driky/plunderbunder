package com.eveonline.crest.requests

import akka.actor.Actor

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

class ThrottledRequest extends Actor {
  def receive = {
    case Get(request) => {
      sender ! request.get()
    }
    case _ => Logger.error("Something else")
  }
}