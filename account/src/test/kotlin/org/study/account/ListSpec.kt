package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty

class ListSpec : StringSpec({
  "list"{
      val list = listOf(1)
      list.subList(1, list.size).shouldBeEmpty()
  }
})