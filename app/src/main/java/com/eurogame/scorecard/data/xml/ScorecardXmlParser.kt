package com.eurogame.scorecard.data.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter

object ScorecardXmlParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): ScorecardTemplate {
        inputStream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readScorecard(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readScorecard(parser: XmlPullParser): ScorecardTemplate {
        parser.require(XmlPullParser.START_TAG, null, "scorecard")

        var game: GameTemplate? = null
        val categories = mutableListOf<CategoryTemplate>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "game" -> {
                    val result = readGame(parser)
                    game = result.first
                    categories.addAll(result.second)
                }
                else -> skip(parser)
            }
        }

        return ScorecardTemplate(
            game = game ?: throw XmlPullParserException("Missing game tag"),
            categories = categories
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readGame(parser: XmlPullParser): Pair<GameTemplate, List<CategoryTemplate>> {
        parser.require(XmlPullParser.START_TAG, null, "game")

        val game = GameTemplate(
            name = parser.getAttributeValue(null, "name") ?: "",
            subtitle = parser.getAttributeValue(null, "subtitle"),
            designer = parser.getAttributeValue(null, "designer"),
            studio = parser.getAttributeValue(null, "studio"),
            minPlayers = parser.getAttributeValue(null, "minPlayers")?.toIntOrNull(),
            maxPlayers = parser.getAttributeValue(null, "maxPlayers")?.toIntOrNull(),
            backgroundImageUrl = parser.getAttributeValue(null, "backgroundImageUrl"),
            publishYear = parser.getAttributeValue(null, "publishYear")?.toIntOrNull()
        )

        val categories = mutableListOf<CategoryTemplate>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "scoreCategory" -> categories.add(readScoreCategory(parser))
                else -> skip(parser)
            }
        }

        return Pair(game, categories)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readScoreCategory(parser: XmlPullParser): CategoryTemplate {
        parser.require(XmlPullParser.START_TAG, null, "scoreCategory")

        val category = CategoryTemplate(
            name = parser.getAttributeValue(null, "name") ?: "",
            description = parser.getAttributeValue(null, "description"),
            iconUrl = parser.getAttributeValue(null, "iconUrl"),
            backgroundImageUrl = parser.getAttributeValue(null, "backgroundImageUrl"),
            scoringRuleType = parser.getAttributeValue(null, "scoringRuleType"),
            scoreIndex = parser.getAttributeValue(null, "scoreIndex")?.toIntOrNull() ?: 0
        )

        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, null, "scoreCategory")

        return category
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    @Throws(IOException::class)
    fun serialize(template: ScorecardTemplate, outputStream: OutputStream) {
        val serializer = Xml.newSerializer()
        serializer.setOutput(outputStream, "UTF-8")
        serializer.startDocument("UTF-8", true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        // Start scorecard
        serializer.startTag(null, "scorecard")

        // Write game with attributes
        serializer.startTag(null, "game")
        serializer.attribute(null, "name", template.game.name)
        template.game.subtitle?.let { serializer.attribute(null, "subtitle", it) }
        template.game.designer?.let { serializer.attribute(null, "designer", it) }
        template.game.studio?.let { serializer.attribute(null, "studio", it) }
        template.game.minPlayers?.let { serializer.attribute(null, "minPlayers", it.toString()) }
        template.game.maxPlayers?.let { serializer.attribute(null, "maxPlayers", it.toString()) }
        template.game.backgroundImageUrl?.let { serializer.attribute(null, "backgroundImageUrl", it) }
        template.game.publishYear?.let { serializer.attribute(null, "publishYear", it.toString()) }

        // Write score categories
        template.categories.sortedBy { it.scoreIndex }.forEach { category ->
            serializer.startTag(null, "scoreCategory")
            serializer.attribute(null, "name", category.name)
            category.description?.let { serializer.attribute(null, "description", it) }
            category.iconUrl?.let { serializer.attribute(null, "iconUrl", it) }
            category.backgroundImageUrl?.let { serializer.attribute(null, "backgroundImageUrl", it) }
            category.scoringRuleType?.let { serializer.attribute(null, "scoringRuleType", it) }
            serializer.attribute(null, "scoreIndex", category.scoreIndex.toString())
            serializer.endTag(null, "scoreCategory")
        }

        serializer.endTag(null, "game")
        serializer.endTag(null, "scorecard")
        serializer.endDocument()
        serializer.flush()
    }

    fun serializeToString(template: ScorecardTemplate): String {
        val writer = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        serializer.startTag(null, "scorecard")
        serializer.startTag(null, "game")
        serializer.attribute(null, "name", template.game.name)
        template.game.subtitle?.let { serializer.attribute(null, "subtitle", it) }
        template.game.designer?.let { serializer.attribute(null, "designer", it) }
        template.game.studio?.let { serializer.attribute(null, "studio", it) }
        template.game.minPlayers?.let { serializer.attribute(null, "minPlayers", it.toString()) }
        template.game.maxPlayers?.let { serializer.attribute(null, "maxPlayers", it.toString()) }
        template.game.backgroundImageUrl?.let { serializer.attribute(null, "backgroundImageUrl", it) }
        template.game.publishYear?.let { serializer.attribute(null, "publishYear", it.toString()) }

        template.categories.sortedBy { it.scoreIndex }.forEach { category ->
            serializer.startTag(null, "scoreCategory")
            serializer.attribute(null, "name", category.name)
            category.description?.let { serializer.attribute(null, "description", it) }
            category.iconUrl?.let { serializer.attribute(null, "iconUrl", it) }
            category.backgroundImageUrl?.let { serializer.attribute(null, "backgroundImageUrl", it) }
            category.scoringRuleType?.let { serializer.attribute(null, "scoringRuleType", it) }
            serializer.attribute(null, "scoreIndex", category.scoreIndex.toString())
            serializer.endTag(null, "scoreCategory")
        }

        serializer.endTag(null, "game")
        serializer.endTag(null, "scorecard")
        serializer.endDocument()
        serializer.flush()

        return writer.toString()
    }
}
