package com.eurogame.scorecard.data.xml

import org.junit.Assert.*
import org.junit.Test
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ScorecardXmlParserTest {

    private val testTemplate = ScorecardTemplate(
        game = GameTemplate(
            name = "Wingspan",
            subtitle = "European Edition",
            designer = "Elizabeth Hargrave",
            studio = "Stonemaier Games",
            minPlayers = 1,
            maxPlayers = 5,
            backgroundImageUrl = "https://example.com/bg.png",
            publishYear = 2019
        ),
        categories = listOf(
            CategoryTemplate(
                name = "Birds",
                description = "Points from bird cards",
                iconUrl = "https://example.com/bird.png",
                backgroundImageUrl = "https://example.com/bird-bg.png",
                scoringRuleType = "sum",
                scoreIndex = 0
            ),
            CategoryTemplate(
                name = "Bonus Cards",
                description = "End of round bonuses",
                iconUrl = "https://example.com/bonus.png",
                backgroundImageUrl = null,
                scoringRuleType = "max",
                scoreIndex = 1
            )
        )
    )

    private val minimalXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <scorecard>
            <game name="TestGame">
                <scoreCategory name="Category1" scoreIndex="0"/>
            </game>
        </scorecard>
    """.trimIndent()

    private val fullXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <scorecard>
            <game name="Wingspan"
                  subtitle="European Edition"
                  designer="Elizabeth Hargrave"
                  studio="Stonemaier Games"
                  minPlayers="1"
                  maxPlayers="5"
                  backgroundImageUrl="https://example.com/bg.png"
                  publishYear="2019">
                <scoreCategory name="Birds"
                               description="Points from bird cards"
                               iconUrl="https://example.com/bird.png"
                               backgroundImageUrl="https://example.com/bird-bg.png"
                               scoringRuleType="sum"
                               scoreIndex="0"/>
                <scoreCategory name="Bonus Cards"
                               description="End of round bonuses"
                               iconUrl="https://example.com/bonus.png"
                               scoringRuleType="max"
                               scoreIndex="1"/>
            </game>
        </scorecard>
    """.trimIndent()

    @Test
    fun `parse reads minimal valid XML`() {
        val inputStream = ByteArrayInputStream(minimalXml.toByteArray())

        val template = ScorecardXmlParser.parse(inputStream)

        assertEquals("TestGame", template.game.name)
        assertEquals(1, template.categories.size)
        assertEquals("Category1", template.categories[0].name)
        assertEquals(0, template.categories[0].scoreIndex)
    }

    @Test
    fun `parse reads full XML with all attributes`() {
        val inputStream = ByteArrayInputStream(fullXml.toByteArray())

        val template = ScorecardXmlParser.parse(inputStream)

        // Verify game attributes
        assertEquals("Wingspan", template.game.name)
        assertEquals("European Edition", template.game.subtitle)
        assertEquals("Elizabeth Hargrave", template.game.designer)
        assertEquals("Stonemaier Games", template.game.studio)
        assertEquals(1, template.game.minPlayers)
        assertEquals(5, template.game.maxPlayers)
        assertEquals("https://example.com/bg.png", template.game.backgroundImageUrl)
        assertEquals(2019, template.game.publishYear)

        // Verify categories
        assertEquals(2, template.categories.size)

        val birds = template.categories[0]
        assertEquals("Birds", birds.name)
        assertEquals("Points from bird cards", birds.description)
        assertEquals("https://example.com/bird.png", birds.iconUrl)
        assertEquals("https://example.com/bird-bg.png", birds.backgroundImageUrl)
        assertEquals("sum", birds.scoringRuleType)
        assertEquals(0, birds.scoreIndex)

        val bonus = template.categories[1]
        assertEquals("Bonus Cards", bonus.name)
        assertEquals("End of round bonuses", bonus.description)
        assertEquals("https://example.com/bonus.png", bonus.iconUrl)
        assertNull(bonus.backgroundImageUrl)
        assertEquals("max", bonus.scoringRuleType)
        assertEquals(1, bonus.scoreIndex)
    }

    @Test
    fun `parse handles missing optional game attributes`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
                <game name="TestGame">
                    <scoreCategory name="Cat1" scoreIndex="0"/>
                </game>
            </scorecard>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        val template = ScorecardXmlParser.parse(inputStream)

        assertEquals("TestGame", template.game.name)
        assertNull(template.game.subtitle)
        assertNull(template.game.designer)
        assertNull(template.game.studio)
        assertNull(template.game.minPlayers)
        assertNull(template.game.maxPlayers)
        assertNull(template.game.backgroundImageUrl)
        assertNull(template.game.publishYear)
    }

    @Test
    fun `parse handles missing optional category attributes`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
                <game name="TestGame">
                    <scoreCategory name="Cat1" scoreIndex="5"/>
                </game>
            </scorecard>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        val template = ScorecardXmlParser.parse(inputStream)

        val category = template.categories[0]
        assertEquals("Cat1", category.name)
        assertNull(category.description)
        assertNull(category.iconUrl)
        assertNull(category.backgroundImageUrl)
        assertNull(category.scoringRuleType)
        assertEquals(5, category.scoreIndex)
    }

    @Test
    fun `parse handles default scoreIndex when missing`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
                <game name="TestGame">
                    <scoreCategory name="Cat1"/>
                </game>
            </scorecard>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        val template = ScorecardXmlParser.parse(inputStream)

        assertEquals(0, template.categories[0].scoreIndex)
    }

    @Test(expected = XmlPullParserException::class)
    fun `parse throws exception when game tag is missing`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
            </scorecard>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        ScorecardXmlParser.parse(inputStream)
    }

    @Test(expected = XmlPullParserException::class)
    fun `parse throws exception when scorecard tag is missing`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <game name="Test">
                <scoreCategory name="Cat1" scoreIndex="0"/>
            </game>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        ScorecardXmlParser.parse(inputStream)
    }

    @Test
    fun `parse handles invalid integer values gracefully`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
                <game name="TestGame" minPlayers="invalid" maxPlayers="not-a-number" publishYear="2019a">
                    <scoreCategory name="Cat1" scoreIndex="bad"/>
                </game>
            </scorecard>
        """.trimIndent()

        val inputStream = ByteArrayInputStream(xml.toByteArray())
        val template = ScorecardXmlParser.parse(inputStream)

        assertNull(template.game.minPlayers)
        assertNull(template.game.maxPlayers)
        assertNull(template.game.publishYear)
        assertEquals(0, template.categories[0].scoreIndex) // Defaults to 0
    }

    @Test
    fun `serialize writes minimal template correctly`() {
        val minimalTemplate = ScorecardTemplate(
            game = GameTemplate(name = "TestGame"),
            categories = listOf(CategoryTemplate(name = "Cat1", scoreIndex = 0))
        )

        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(minimalTemplate, outputStream)

        val xml = outputStream.toString("UTF-8")

        assertTrue(xml.contains("<scorecard>"))
        assertTrue(xml.contains("<game"))
        assertTrue(xml.contains("name=\"TestGame\""))
        assertTrue(xml.contains("<scoreCategory"))
        assertTrue(xml.contains("name=\"Cat1\""))
        assertTrue(xml.contains("scoreIndex=\"0\""))
        assertTrue(xml.contains("</scorecard>"))
    }

    @Test
    fun `serialize writes full template correctly`() {
        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(testTemplate, outputStream)

        val xml = outputStream.toString("UTF-8")

        // Verify game attributes
        assertTrue(xml.contains("name=\"Wingspan\""))
        assertTrue(xml.contains("subtitle=\"European Edition\""))
        assertTrue(xml.contains("designer=\"Elizabeth Hargrave\""))
        assertTrue(xml.contains("studio=\"Stonemaier Games\""))
        assertTrue(xml.contains("minPlayers=\"1\""))
        assertTrue(xml.contains("maxPlayers=\"5\""))
        assertTrue(xml.contains("backgroundImageUrl=\"https://example.com/bg.png\""))
        assertTrue(xml.contains("publishYear=\"2019\""))

        // Verify categories
        assertTrue(xml.contains("name=\"Birds\""))
        assertTrue(xml.contains("description=\"Points from bird cards\""))
        assertTrue(xml.contains("iconUrl=\"https://example.com/bird.png\""))
        assertTrue(xml.contains("scoringRuleType=\"sum\""))
        assertTrue(xml.contains("name=\"Bonus Cards\""))
    }

    @Test
    fun `serialize omits null optional game attributes`() {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "TestGame"),
            categories = listOf(CategoryTemplate(name = "Cat1", scoreIndex = 0))
        )

        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(template, outputStream)

        val xml = outputStream.toString("UTF-8")

        assertFalse(xml.contains("subtitle="))
        assertFalse(xml.contains("designer="))
        assertFalse(xml.contains("studio="))
        assertFalse(xml.contains("minPlayers="))
        assertFalse(xml.contains("maxPlayers="))
        assertFalse(xml.contains("backgroundImageUrl="))
        assertFalse(xml.contains("publishYear="))
    }

    @Test
    fun `serialize omits null optional category attributes`() {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "TestGame"),
            categories = listOf(
                CategoryTemplate(
                    name = "Cat1",
                    description = null,
                    iconUrl = null,
                    backgroundImageUrl = null,
                    scoringRuleType = null,
                    scoreIndex = 0
                )
            )
        )

        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(template, outputStream)

        val xml = outputStream.toString("UTF-8")

        assertTrue(xml.contains("name=\"Cat1\""))
        assertFalse(xml.contains("description="))
        assertFalse(xml.contains("iconUrl="))
        assertFalse(xml.contains("backgroundImageUrl="))
        assertFalse(xml.contains("scoringRuleType="))
        assertTrue(xml.contains("scoreIndex=\"0\""))
    }

    @Test
    fun `serialize sorts categories by scoreIndex`() {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "TestGame"),
            categories = listOf(
                CategoryTemplate(name = "Cat3", scoreIndex = 2),
                CategoryTemplate(name = "Cat1", scoreIndex = 0),
                CategoryTemplate(name = "Cat2", scoreIndex = 1)
            )
        )

        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(template, outputStream)

        val xml = outputStream.toString("UTF-8")

        val cat1Index = xml.indexOf("name=\"Cat1\"")
        val cat2Index = xml.indexOf("name=\"Cat2\"")
        val cat3Index = xml.indexOf("name=\"Cat3\"")

        assertTrue(cat1Index < cat2Index)
        assertTrue(cat2Index < cat3Index)
    }

    @Test
    fun `serializeToString returns valid XML string`() {
        val xml = ScorecardXmlParser.serializeToString(testTemplate)

        assertTrue(xml.contains("<?xml"))
        assertTrue(xml.contains("<scorecard>"))
        assertTrue(xml.contains("name=\"Wingspan\""))
        assertTrue(xml.contains("</scorecard>"))
    }

    @Test
    fun `serialize and parse are symmetric for minimal template`() {
        val originalTemplate = ScorecardTemplate(
            game = GameTemplate(name = "TestGame"),
            categories = listOf(CategoryTemplate(name = "Cat1", scoreIndex = 0))
        )

        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(originalTemplate, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val parsedTemplate = ScorecardXmlParser.parse(inputStream)

        assertEquals(originalTemplate.game.name, parsedTemplate.game.name)
        assertEquals(originalTemplate.categories.size, parsedTemplate.categories.size)
        assertEquals(originalTemplate.categories[0].name, parsedTemplate.categories[0].name)
    }

    @Test
    fun `serialize and parse are symmetric for full template`() {
        val outputStream = ByteArrayOutputStream()
        ScorecardXmlParser.serialize(testTemplate, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val parsedTemplate = ScorecardXmlParser.parse(inputStream)

        // Verify game
        assertEquals(testTemplate.game.name, parsedTemplate.game.name)
        assertEquals(testTemplate.game.subtitle, parsedTemplate.game.subtitle)
        assertEquals(testTemplate.game.designer, parsedTemplate.game.designer)
        assertEquals(testTemplate.game.studio, parsedTemplate.game.studio)
        assertEquals(testTemplate.game.minPlayers, parsedTemplate.game.minPlayers)
        assertEquals(testTemplate.game.maxPlayers, parsedTemplate.game.maxPlayers)
        assertEquals(testTemplate.game.backgroundImageUrl, parsedTemplate.game.backgroundImageUrl)
        assertEquals(testTemplate.game.publishYear, parsedTemplate.game.publishYear)

        // Verify categories
        assertEquals(testTemplate.categories.size, parsedTemplate.categories.size)
        for (i in testTemplate.categories.indices) {
            val original = testTemplate.categories[i]
            val parsed = parsedTemplate.categories[i]
            assertEquals(original.name, parsed.name)
            assertEquals(original.description, parsed.description)
            assertEquals(original.iconUrl, parsed.iconUrl)
            assertEquals(original.backgroundImageUrl, parsed.backgroundImageUrl)
            assertEquals(original.scoringRuleType, parsed.scoringRuleType)
            assertEquals(original.scoreIndex, parsed.scoreIndex)
        }
    }
}
