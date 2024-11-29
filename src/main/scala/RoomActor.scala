package fr.cytech.icc

import java.time.OffsetDateTime
import java.util.UUID
import scala.collection.immutable.SortedSet

import org.apache.pekko.actor.typed.{ ActorRef, Behavior }
import org.apache.pekko.actor.typed.scaladsl.Behaviors

enum Message {
  case CreatePost(author: String, content: String)
  case ListPosts(replyTo: ActorRef[SortedSet[Post]])
  case LatestPost(replyTo: ActorRef[Option[Post]])
  case GetPost(id: UUID, replyTo: ActorRef[Option[Post]])
}

case class Post(id: UUID, author: String, postedAt: OffsetDateTime, content: String)

given Ordering[Post] = Ordering.by(_.postedAt)

case class RoomActor(name: String) {

  private def handle(posts: SortedSet[Post]): Behavior[Message] = {
    Behaviors.receiveMessage {
      // crée un post et actualise l'état de l'acteur (ensemble des posts)
      case Message.CreatePost(author, content) =>
        val post = Post(UUID.randomUUID(), author, OffsetDateTime.now(), content)
        handle(posts + post)
      // renvoie l'ensemble des pots au RoomListActor et garde l'état actuel
      case Message.ListPosts(replyTo) =>
        replyTo ! posts
        Behaviors.same
      // renvoie le dernier post au RoomListActor et garde l'éat actuel
      case Message.LatestPost(replyTo) =>
        if(!posts.isEmpty) {
          val post = posts.last
          replyTo ! Some(post)
        }
        else {
          replyTo ! None
        }
        Behaviors.same
      // renvoie une Option du post correspondant au RoomlistActor et garde l'état actuel
      case Message.GetPost(id, replyTo) =>
        val post = posts.find(_.id == id)
        replyTo ! post
        Behaviors.same
    }
  }
}

object RoomActor {
  def apply(name: String): Behavior[Message] = new RoomActor(name).handle(SortedSet.empty)
}
