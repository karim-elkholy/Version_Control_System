package svcs

import java.io.File
import java.nio.file.Paths

fun getCommands(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    // map["config"] = "Get and set a username."
    // map["add"] = "Add a file to the index."
    map["log"] = "Show commit logs."
    map["commit"] = "Save changes."
    map["checkout"] = "Restore a file."

    return map
}

fun printHelp() {
    val helpMsg = """
    These are SVCS commands:
    config     Get and set a username.
    add        Add a file to the index.
    log        Show commit logs.
    commit     Save changes.
    checkout   Restore a file.
    """.trimIndent()

    println(helpMsg)
}

//Path to the vcs root directory
val rootVCSDir = File(".")
    .absoluteFile.parentFile.resolve("vcs")

//Path to the index file
val indexFile = File("$rootVCSDir/index.txt")
val logFile = File("$rootVCSDir/log.txt")
val configFile = File("$rootVCSDir/config.txt")
val commitsDir = File("$rootVCSDir/commits")

object VersionControlSystem {
    init {

        //Ensure the log file exists
        if ( !logFile.exists() ) { logFile.writeText("") }
    }

    fun checkout(commitID: String = "") : String
    {
        //If no commit ID was passed
        if ( commitID == "" ) { return "Commit id was not passed." }

        //Get a list of commits
        val commits = listCommits()

        //If no commit exists with the given ID
        if ( !commits.contains(commitID)) { return "Commit does not exist." }

        val specificCommitDir = commitsDir.resolve(commitID)

        //Iterate through all the files in the commit directory
        specificCommitDir.walkBottomUp().forEach {
            //Create the relative path
            val relativePath = it.path.replace(
                specificCommitDir.path,
                ""
            )

            //If the file is within the commit directory
            if ( relativePath.isNotEmpty())
            {
                //Copy it
                it.copyTo(
                    Paths.get(
                        rootVCSDir.parentFile.path,
                        relativePath
                    ).toFile(),
                    overwrite = true
                )
            }

            // the root directory
            // the root directory

        }

        //Return to the given commit
        return "Switched to commit ${commitID}."
    }

    fun commit(commitMsg: String = "") : String
    {
        //Return early if commit message is empty
        if ( commitMsg == "" ) { return "Message was not passed."}

        //Get the author name
        val authorName = config()

        //Get a list of all the commits
        val commits = listCommits()

        //If there is at least one commit
        if (commits.isNotEmpty()) {

            //If the commit is up-to-date
            if ( isWorkingDirectoryClean(commits[0]) ) { return "Nothing to commit." }
        }

        //Create the commit for this file
        val commitHash = "${indexFile.readBytes().hashCode()}${commitMsg.hashCode()}"

        //Holds the content to be written to the log file
        var logFileContent = """
        commit $commitHash
        Author: $authorName
        $commitMsg
        """.trimIndent()

        //Create a directory with the given hash
        val newCommitDir = commitsDir.resolve(commitHash)

        //Copy the list of tracked files to the new commits directory
        getTrackedFiles().forEach {
            File(it).copyTo(newCommitDir.resolve(it))
        }

        //Append the existing log file content if the log file already exists
        if ( logFile.exists() ) { logFileContent += "\n${logFile.readText()}" }

        //Update the log file
        logFile.writeText(logFileContent)

        //Indicate the changes were committed
        return "Changes are committed."
    }

    /**
     * Lists all commits.
     *
     * @return Returns a list of commits .
     */
    private fun listCommits(): List<String>
    {
        //If the log file exists
        if ( logFile.exists() ) {

            //Read the content of the log file
            val logFileContent = logFile.readText()

            //Return the list of commits(if any)
            if ( logFileContent != "" ) {
                return Regex("commit (.*)")
                    .findAll(logFileContent)
                    .map { it.groupValues[1] }
                    .toList()
            }
        }

        //Return an empty list since there are no commits yet
        return listOf()
    }

    /**
     * Gets a list of tracked files.
     *
     * @return Returns a list of tracked files.
     */
    private fun getTrackedFiles(): List<String>
    {
        //If the index file exists
        if ( indexFile.exists() ) {

            //Read the content of the index file
            val indexFileContent = indexFile.readText().trim()

            //Return the list of files tracked(if any)
            if ( indexFileContent != "" ) { return indexFileContent.split("\n") }
        }

        //Return an empty list since no files are tracked
        return listOf()
    }

    /**
     * Checks whether the working directory is clean compared to a specific commit.
     *
     * @param commitName Commit to compare the working directory against.
     * @return Returns True if the project directory is up-to-date compared to the specified commit otherwise false.
     */
    private fun isWorkingDirectoryClean(commitName: String): Boolean
    {
        //Get a list of currently tracked files
        val trackedFiles = getTrackedFiles()

        //Iterate through all the files in this directory
        trackedFiles.forEach {

            //Create the path to this file within the commit
            val commitFilepath = commitsDir
                .resolve(commitName)
                .resolve(it)

            //Return false if the commit does not contain this file indicating a dirty working directory
            if ( !commitFilepath.exists() ) { return false }

            //Read the content of both files
            val stageFile = File(it).readBytes()
            val committedFile = commitFilepath.readBytes()

            //Return false if both files are not the same indicating a dirty working directory
            if ( !stageFile.contentEquals(committedFile)) { return false }
        }

        //Return true if the files in the working tree and the last commit are the same
        return true
    }

    fun log(): Boolean {

        //If the log file exists
        if ( logFile.exists() ) {

            //Read the content of the log file
            val logFileContent = logFile.readText()

            //At least one commit is present since the log file exists and is not empty
            if ( logFileContent != "" ) { println(logFileContent); return true }
        }

        //No commits exist if the log file is empty or does not exist
        println("No commits yet.")

        //Return false since no commits exist so there is no log
        return false
    }
}

/**
 * Adds a new file to be tracked.
 *
 * @param newFile Path of the file to track relative to the project root.
 */
fun add(newFile: String) {

    // If a new file should be tracked
    if (newFile != "")
    {
        //If the file does not exist
        if (!File(newFile).exists())
        {
            println("Can't find '$newFile'.")

        } else
        {

            indexFile.appendText("$newFile\n")
            println("The file '$newFile' is tracked.")
        }

    } else
    {
        // Print the current tracked files if they exist
        if (indexFile.exists())
        {
            val currentTrackedFiles = indexFile.readText()
            println("Tracked files:")
            println(currentTrackedFiles)

            // Prompt the user to add a file
        } else {
            println("Add a file to the index.")
        }
    }
}

fun config(newUsername: String = "") : String {

    //Make sure to create the log file

    // Update the config file with the new username
    if (newUsername != "") { configFile.writeText(newUsername) }

    //Holds the username
    var username = ""

    // Read the username from the config file(if it exists)
    if (configFile.exists()) { username = configFile.readText() }

    //Return the username
    return username
}

fun configCli(newUsername: String) {

    //Get the current username
    val username = config(newUsername)

    // If the username is empty
    if ( username == "" )
    {
        //Ask the user to identify themselves
        println("Please, tell me who you are.")

        // If the username is empty
    } else
    {
        //Print the stored username
        println("The username is $username.")
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) { printHelp() } else if (args.size == 1 && args[0] == "--help") { printHelp() } else {
        // Create the vcs directory
        rootVCSDir.mkdirs()

        // Get the user command
        when (val userCommand = args[0]) {
            "config" -> {
                // Set the new username
                val newUsername = if (args.size > 1) args[1] else ""
                configCli(newUsername)
            }
            "add" -> {
                // Set the new username
                val newUsername = if (args.size > 1) args[1] else ""
                add(newUsername)
            }
            "log" -> {
                VersionControlSystem.log()
            }

            "commit" -> {
                val commitMsg = if (args.size > 1) args[1] else ""

                //Commit the changes
                println( VersionControlSystem.commit(commitMsg) )
            }
            "checkout" -> {
                val commitID = if (args.size > 1) args[1] else ""

                //Commit the changes
                println( VersionControlSystem.checkout(commitID) )
            }
            else -> {
                // Get the command description
                val commandMsg = getCommands().getOrDefault(
                    userCommand,
                    "'$userCommand' is not a SVCS command."
                )

                // Print the appropriate message
                println(commandMsg)
            }
        }
    }
}
