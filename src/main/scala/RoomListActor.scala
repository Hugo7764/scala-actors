package fr.cytech.icc

import org.apache.pekko.actor.typed.{ ActorRef, Behavior }
import org.apache.pekko.actor.typed.scaladsl.Behaviors

enum RoomListMessage {
  case CreateRoom(name: String)
  case GetRoom(name: String, replyTo: ActorRef[Option[ActorRef[Message]]])
}

object RoomListActor {

  import RoomListMessage.*

  def apply(rooms: Map[String, ActorRef[Message]] = Map.empty): Behavior[RoomListMessage] = {
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        /* crée une room si une identique n'existe pas
         * et change l'état de l'acteur avec la Map actualisée
         */
        case CreateRoom(name)       => 
          if(!rooms.contains(name)) {
            val room = context.spawn(RoomActor(name), name)
            apply(rooms + (name -> room))
          }
          else Behaviors.same
        // renvoie la room correspondant au nom donné si elle existe et garde l'état actuel
        case GetRoom(name, replyTo) => 
          if(rooms.contains(name)) {
            val room = rooms.get(name)
            replyTo ! room
          }
          else {
            replyTo ! None
          }
          Behaviors.same
      }
    }
  }
}
