package corla.agent

import corla.memory.Memory3
import corla.memory.Memory3.Next
import corla.misc.PDF
import corla.Reward

import scalaz._
import syntax.monad._

import scalaz.Traverse

case class LinCombiAgent[S,A,P[_]:PDF](m: P[Agent[S,A,P]])
  extends Agent.Aux[S,A,P[Agent[S,A,P]],P] {
  val M = new Memory3[M, S, A] {
    def addExperience: ((S, A, Reward, Next[S, A])) => (M) => M =
      e => m => m.map(agent => Agent(agent.π, agent.M.addExperience.apply(e)(agent.m))(agent.M))

    def addBatchExperience[F[_] : Traverse]: F[(S, A, Reward, Next[S, A])] => M => M =
      fe => m => m.map(agent => Agent(agent.π, agent.M.addBatchExperience[F].apply(fe)(agent.m))(agent.M))
  }

  def π: (M,S) => P[A] = (m,s) => m.flatMap(a => a.π(a.m,s))
}
