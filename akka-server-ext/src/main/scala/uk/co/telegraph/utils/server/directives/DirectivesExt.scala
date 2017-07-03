package uk.co.telegraph.utils.server.directives

trait DirectivesExt extends CorsDirective
  with UuidDirectives
  with LogDirectives
  with FlowDirectives

object DirectivesExt extends DirectivesExt
