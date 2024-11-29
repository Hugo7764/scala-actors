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
      case Message.CreatePost(author, content) =>
        val post = Post(UUID.randomUUID(), author, OffsetDateTime.now(), content)
        handle(posts + post)
      case Message.ListPosts(replyTo) =>
        replyTo ! posts
        Behaviors.same
      case Message.LatestPost(replyTo) =>
        if(!posts.isEmpty) {
          val post = posts.last
          replyTo ! Some(post)
        }
        else {
          replyTo ! None
        }
        Behaviors.same
      case Message.GetPost(id, replyTo) =>
        ???

    }
  }
}

object RoomActor {
  def apply(name: String): Behavior[Message] = new RoomActor(name).handle(SortedSet.empty)
}
