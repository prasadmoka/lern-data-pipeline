package org.sunbird.job.util

import org.scalatest.{FlatSpec, Matchers}

class FileUtilsSpec extends FlatSpec with Matchers {

	"getBasePath with empty identifier" should "return the path" in {
		val result = FileUtils.getBasePath("")
		result.nonEmpty shouldBe(true)
	}
}
