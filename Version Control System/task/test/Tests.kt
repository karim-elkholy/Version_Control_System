import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.exception.outcomes.WrongAnswer
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram
import java.io.File
import java.io.File.separatorChar
import java.io.IOException
import kotlin.random.Random

// version 1.2
class TestStage4 : StageTest<String>() {

    @DynamicTest(order = 1)
    fun checkVcsDirAndFileExistsAfterConfigCommand(): CheckResult {
        deleteVcsDir()
        val dir = File("vcs")
        val configPath = "vcs${separatorChar}config.txt"
        val config =  File(configPath)
        val testFeedback = "\n\nMake sure vcs folder and $configPath file are being created by the program."
        val dirNotFoundMessage = "Could not find vcs folder after executing config command.$testFeedback"
        val configNotFoundMessage = "Could not find $configPath after executing config command.$testFeedback"
        val fileNotFoundMessage = "Your program has thrown some IOException.$testFeedback"

        try {
            val testedProgram = TestedProgram()
            testedProgram.feedbackOnException(java.io.IOException::class.java, fileNotFoundMessage)
            testedProgram.start("config", "Max")

            when {
                dir.exists().not() || dir.isDirectory.not() -> return CheckResult.wrong(dirNotFoundMessage)
                config.exists().not() -> return CheckResult.wrong(configNotFoundMessage)
            }

        } finally {
            deleteVcsDir()
        }
        return CheckResult.correct()
    }

    @DynamicTest(order = 2)
    fun checkVcsDirAndFileExistsAfterAddCommand(): CheckResult {

        val dir = File("vcs")
        val indexPath = "vcs${separatorChar}index.txt"
        val index =  File(indexPath)
        val testFeedback = "\n\nMake sure vcs folder and $indexPath file are being created by the program."
        val dirNotFoundMessage = "Could not find vcs folder after executing add command.$testFeedback"
        val configNotFoundMessage = "Could not find $indexPath after executing add command.$testFeedback"
        val fileNotFoundMessage = "Your program has thrown some IOException.$testFeedback"

        val abcFile = File("abc.txt")
        abcFile.createNewFile()

        try {
            val testedProgram = TestedProgram()
            testedProgram.feedbackOnException(java.io.IOException::class.java, fileNotFoundMessage)
            testedProgram.start("add", abcFile.name)

            when {
                dir.exists().not() || dir.isDirectory.not() -> return CheckResult.wrong(dirNotFoundMessage)
                index.exists().not() -> return CheckResult.wrong(configNotFoundMessage)
            }

        } finally {
            deleteVcsDir()
            deleteFiles(abcFile)
        }
        return CheckResult.correct()
    }

    @DynamicTest(order = 3)
    fun helpPageTest(): CheckResult {
        try {
            checkHelpPageOutput(TestedProgram().start())
            checkHelpPageOutput(TestedProgram().start("--help"))
        } finally {
            deleteVcsDir()
        }
        return CheckResult.correct()
    }

    @DynamicTest(order = 4)
    fun configTest(): CheckResult {
        try {
            checkOutputString(TestedProgram().start("config"), "Please, tell me who you are.")
            checkOutputString(TestedProgram().start("config", "Max"), "The username is Max.")
            checkOutputString(TestedProgram().start("config"), "The username is Max.")
            checkOutputString(TestedProgram().start("config", "John"), "The username is John.")
            checkOutputString(TestedProgram().start("config"), "The username is John.")
        } finally {
            deleteVcsDir()
        }

        return CheckResult.correct()
    }

    @DynamicTest(order = 5)
    fun addTest(): CheckResult {
        val fileName1 = "file${Random.nextInt(0, 1000)}.txt"
        val fileName2= "file${Random.nextInt(0, 1000)}.txt"

        val file1 = File(fileName1)
        val file2 = File(fileName2)
        file1.createNewFile()
        file2.createNewFile()

        try {
            checkOutputString(TestedProgram().start("add"), "Add a file to the index.")
            checkOutputString(TestedProgram().start("add", fileName1), "The file '$fileName1' is tracked.")
            checkOutputString(TestedProgram().start("add"), "Tracked files:\n$fileName1")
            checkOutputString(TestedProgram().start("add", fileName2), "The file '$fileName2' is tracked.")
            checkOutputString(TestedProgram().start("add"), "Tracked files:\n$fileName1\n$fileName2")

            val notExistsFileName = "file${Random.nextInt(0, 1000)}.txt"
            checkOutputString(
                TestedProgram().start("add", notExistsFileName),
                "Can't find '$notExistsFileName'."
            )
        } finally {
            deleteVcsDir()
            deleteFiles(file1, file2)
        }

        return CheckResult.correct()
    }

    @DynamicTest(order = 6)
    fun checkCommitsDirAndLogFileExistsAfterCommitCommand(): CheckResult {
        val file1 = File("first_file.txt")
        val file2 = File("second_file.txt")

        file1.writeText("some test data for the first file")
        file2.writeText("some test data for the second file")

        val commitsDirPath = "vcs${separatorChar}commits"
        val commitsDir = File(commitsDirPath)
        val logFilePath = "vcs${separatorChar}log.txt"
        val logFile = File(logFilePath)

        val testFeedback = "\n\nMake sure $commitsDirPath folder and $logFilePath file are being created by the program."
        val commitsDirNotFoundMessage = "Could not find $commitsDirPath folder after executing config command.$testFeedback"
        val logNotFoundMessage = "Could not find $logFilePath after executing config command.$testFeedback"
        val fileNotFoundMessage = "Your program has thrown some IOException.$testFeedback"

        try {
            val username = getRandomUserName()

            TestedProgram().start("config", username)
            TestedProgram().start("add", file1.name)
            TestedProgram().start("add", file2.name)

            val commitProgram = TestedProgram()
            commitProgram.feedbackOnException(java.io.IOException::class.java, fileNotFoundMessage)
            commitProgram.start("commit", "Test message")

            when{
                commitsDir.exists().not() || commitsDir.isDirectory.not() ->
                    return CheckResult.wrong(commitsDirNotFoundMessage)
                logFile.exists().not() -> return CheckResult.wrong(logNotFoundMessage)
            }

        } finally {
            deleteVcsDir()
            deleteFiles(file1, file2)
        }

        return CheckResult.correct()
    }

    @DynamicTest(order = 7)
    fun commitAndLogTest(): CheckResult {
        val file1 = File("first_file.txt")
        val file2 = File("second_file.txt")

        file1.writeText("some test data for the first file")
        file2.writeText("some test data for the second file")

        try {
            val username = getRandomUserName()

            TestedProgram().start("config", username)
            TestedProgram().start("add", file1.name)
            TestedProgram().start("add", file2.name)

            checkOutputString(TestedProgram().start("log"), "No commits yet.")
            checkOutputString(TestedProgram().start("commit"), "Message was not passed.")
            checkOutputString(TestedProgram().start("commit", "Test message"), "Changes are committed.")

            var got = TestedProgram().start("log")
            var want = "commit [commit id]\n" +
                    "Author: $username\n" +
                    "Test message"

            var regex = Regex(
                "commit [^\\s]+\n" +
                        "Author: $username\n" +
                        "Test message", RegexOption.IGNORE_CASE
            )
            checkLogOutput(got, want, regex)

            checkOutputString(TestedProgram().start("commit", "Test message2"), "Nothing to commit.")

            file2.appendText("some text")
            checkOutputString(TestedProgram().start("commit", "Test message3"), "Changes are committed.")

            got = TestedProgram().start("log")
            want = "commit [commit id]\n" +
                    "Author: $username\n" +
                    "Test message3\n\n" +
                    "commit [commit id]\n" +
                    "Author: $username\n" +
                    "Test message"
            regex = Regex(
                "commit [^\\s]+\n" +
                        "Author: $username\n" +
                        "Test message3\n" +
                        "commit [^\\s]+\n" +
                        "Author: $username\n" +
                        "Test message", RegexOption.IGNORE_CASE
            )
            checkLogOutput(got, want, regex)
            checkUniqueCommitHashes(got)

            val commitHashes = parseCommitHashes(got)
            commitHashes.forEach { commitHash ->

                val commitDirPath = "vcs${separatorChar}commits$separatorChar$commitHash"
                val commitDir = File(commitDirPath)
                val versionedFile1 = commitDir.resolve(file1.name)
                val versionedFile2 = commitDir.resolve(file2.name)
                val feedbackMessage = "\n\nMake sure you make versions of tracked files on a folder named with the commitId"

                when{
                    commitDir.exists().not() || commitDir.isDirectory.not() ->
                        return CheckResult.wrong("Could not find folder $commitDirPath$feedbackMessage")

                    versionedFile1.exists().not() ->
                        return CheckResult.wrong("Could not find file ${versionedFile1.name} on $commitDirPath$feedbackMessage")

                    versionedFile2.exists().not() ->
                        return CheckResult.wrong("Could not find file ${versionedFile2.name} on $commitDirPath$feedbackMessage")
                }
            }

        } finally {
            deleteVcsDir()
            deleteFiles(file1, file2)
        }

        return CheckResult.correct()
    }

    @DynamicTest(order = 8)
    fun checkoutTest(): CheckResult {
        val file1 = File("first_file.txt")
        val file2 = File("second_file.txt")
        val untrackedFile = File("untracked_file.txt")

        file1.createNewFile()
        file2.createNewFile()
        untrackedFile.createNewFile()

        try {
            val username = getRandomUserName()

            TestedProgram().start("config", username)
            TestedProgram().start("add", file1.name)
            TestedProgram().start("add", file2.name)

            val initialContentFile1 = "some text in the first file"
            val initialContentFile2 = "some text in the second file"
            val contentUntrackedFile = "some text for the untracked file"

            file1.writeText(initialContentFile1)
            file2.writeText(initialContentFile2)
            untrackedFile.writeText(contentUntrackedFile)

            TestedProgram().start("commit", "First commit")


            val changedContentFile1 = "some changed text in the first file"
            val changedContentFile2 = "some changed text in the second file"
            file1.writeText(changedContentFile1)
            file2.writeText(changedContentFile2)

            TestedProgram().start("commit", "Second commit")

            checkOutputString(TestedProgram().start("checkout"), "Commit id was not passed.")
            checkOutputString(TestedProgram().start("checkout", "wrongId"), "Commit does not exist.")

            val firstCommitHash = parseCommitHashes(TestedProgram().start("log")).last()

            checkOutputString(
                TestedProgram().start("checkout", firstCommitHash),
                "Switched to commit $firstCommitHash."
            )

            if (file1.readText() != initialContentFile1 || file2.readText() != initialContentFile2) {
                throw WrongAnswer(
                    "Wrong content of the tracked files after checkout"
                )
            }

            if (untrackedFile.readText() != contentUntrackedFile) {
                throw WrongAnswer(
                    "Your program changed untracked file"
                )
            }

        } finally {
            deleteVcsDir()
            deleteFiles(file1, file2, untrackedFile)
        }

        return CheckResult.correct()
    }


    @DynamicTest(order = 9)
    fun wrongArgTest(): CheckResult {
        try {
            val suffix = Random.nextInt(0,1000)
            checkOutputString(TestedProgram().start("wrongArg$suffix"), "'wrongArg$suffix' is not a SVCS command.")
        } finally {
            deleteVcsDir()
        }
        return CheckResult.correct()
    }

    private fun prepareString(s: String) =
        s.trim().split(" ").filter { it.isNotBlank() }.joinToString(" ")

    private fun prepareLogOutput(s: String) =
        prepareString(s).trim().split('\n').filter { it.isNotBlank() }.joinToString("\n")

    private fun checkHelpPageOutput(got: String) {
        val helpPage = "These are SVCS commands:\n" +
                "config     Get and set a username.\n" +
                "add        Add a file to the index.\n" +
                "log        Show commit logs.\n" +
                "commit     Save changes.\n" +
                "checkout   Restore a file."

        if (got.isBlank()) {
            throw WrongAnswer(
                "Your program should output:\n$helpPage\n\n" +
                        "But printed nothing"
            )
        } else if (!prepareString(got).equals(prepareString(helpPage), true)) {
            throw WrongAnswer(
                "Your program should output:\n$helpPage\n\n" +
                        "But printed:\n$got"
            )
        }
    }


    private fun checkLogOutput(got: String, want: String, regex: Regex) {
        if (got.isBlank()) {
            throw WrongAnswer(
                "Your program printed nothing"
            )
        } else if (!prepareLogOutput(got).contains(regex)) {
            throw WrongAnswer(
                "Your program should output:\n\"$want\",\n" +
                        "but printed:\n\"$got\""
            )
        }
    }

    private fun parseCommitHashes(logOutput: String): List<String> {
        val regex = Regex(
            "commit ([^\\s]+)", RegexOption.IGNORE_CASE
        )

        return regex.findAll(logOutput).map { it.groupValues[1] }.toList()
    }

    private fun checkUniqueCommitHashes(got: String) {
        val commitHashes = parseCommitHashes(got)

        if (commitHashes.size != commitHashes.toSet().size) {
            throw WrongAnswer(
                "Commit ids are not unique"
            )
        }
    }

    private fun checkOutputString(got: String, want: String) {
        if (got.isBlank()) {
            throw WrongAnswer(
                "Your program should output \"$want\",\n" +
                        "but printed nothing"
            )
        } else if (!prepareString(got).equals(want, true)) {
            throw WrongAnswer(
                "Your program should output \"$want\",\n" +
                        "but printed: \"$got\""
            )
        }
    }

    private fun getRandomUserName() =
        listOf("Marie", "Anna", "Diane", "Sofie", "Christine").random() + Random.nextInt(1000)

    private fun deleteVcsDir() {
        val dir = File("vcs")
        try {
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (ex: IOException) { ex.printStackTrace() }
    }

    private fun deleteFiles(vararg files: File) {
        for(file in files) {
            try { file.delete() } catch (ex: IOException){ ex.printStackTrace() }
        }
    }
}
