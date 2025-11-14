package com.eurogame.scorecard.presentation.templatebrowser

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.eurogame.scorecard.data.storage.TemplateInfo
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.data.xml.CategoryTemplate
import com.eurogame.scorecard.data.xml.GameTemplate
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateBrowserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var storageManager: TemplateStorageManager
    private lateinit var viewModel: TemplateBrowserViewModel

    private val testTemplateInfo1 = TemplateInfo(
        fileName = "wingspan.xml",
        gameName = "Wingspan",
        lastModified = 1000L
    )

    private val testTemplateInfo2 = TemplateInfo(
        fileName = "azul.xml",
        gameName = "Azul",
        lastModified = 2000L
    )

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
                backgroundImageUrl = null,
                scoringRuleType = "sum",
                scoreIndex = 0
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storageManager = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads templates on creation`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1, testTemplateInfo2)

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.templates.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `initial state sorts templates by last modified descending`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1, testTemplateInfo2)

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        // testTemplateInfo2 has lastModified=2000, should be first
        assertEquals("azul.xml", state.templates[0].fileName)
        assertEquals("wingspan.xml", state.templates[1].fileName)
    }

    @Test
    fun `initial state handles empty template list`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns emptyList()

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.templates.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `initial state handles storage error`() = runTest {
        coEvery { storageManager.getAllTemplates() } throws Exception("Storage error")

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.templates.isEmpty())
        assertTrue(state.error?.contains("Failed to load templates") == true)
        assertTrue(state.error?.contains("Storage error") == true)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTemplates loads and sorts templates`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1, testTemplateInfo2)

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.loadTemplates()
        advanceUntilIdle()

        coVerify(exactly = 2) { storageManager.getAllTemplates() }
        assertEquals(2, viewModel.state.value.templates.size)
    }

    @Test
    fun `loadTemplates sets isLoading during operation`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns emptyList()

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        coEvery { storageManager.getAllTemplates() } coAnswers {
            kotlinx.coroutines.delay(100)
            listOf(testTemplateInfo1)
        }

        viewModel.loadTemplates()

        assertTrue(viewModel.state.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `selectTemplate loads template successfully`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.loadTemplate("wingspan.xml") } returns testTemplate

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.selectTemplate("wingspan.xml")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.selectedTemplate)
        assertEquals("Wingspan", viewModel.state.value.selectedTemplate?.game?.name)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `selectTemplate handles template not found`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.loadTemplate("nonexistent.xml") } returns null

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.selectTemplate("nonexistent.xml")
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedTemplate)
    }

    @Test
    fun `selectTemplate handles loading error`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.loadTemplate("wingspan.xml") } throws Exception("Parse error")

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.selectTemplate("wingspan.xml")
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedTemplate)
        assertTrue(viewModel.state.value.error?.contains("Failed to load template") == true)
        assertTrue(viewModel.state.value.error?.contains("Parse error") == true)
    }

    @Test
    fun `showDeleteConfirmation sets deleteConfirmation`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns emptyList()

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        assertNull(viewModel.state.value.deleteConfirmation)

        viewModel.showDeleteConfirmation("wingspan.xml")

        assertEquals("wingspan.xml", viewModel.state.value.deleteConfirmation)
    }

    @Test
    fun `cancelDelete clears deleteConfirmation`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns emptyList()

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.showDeleteConfirmation("wingspan.xml")
        assertEquals("wingspan.xml", viewModel.state.value.deleteConfirmation)

        viewModel.cancelDelete()

        assertNull(viewModel.state.value.deleteConfirmation)
    }

    @Test
    fun `deleteTemplate deletes successfully and reloads templates`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1, testTemplateInfo2)
        coEvery { storageManager.deleteTemplate("wingspan.xml") } returns true

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        // After initial load, mock returns only one template
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo2)

        viewModel.deleteTemplate("wingspan.xml")
        advanceUntilIdle()

        coVerify { storageManager.deleteTemplate("wingspan.xml") }
        coVerify(exactly = 2) { storageManager.getAllTemplates() } // Initial + after delete
        assertNull(viewModel.state.value.deleteConfirmation)
        assertEquals(1, viewModel.state.value.templates.size)
    }

    @Test
    fun `deleteTemplate handles delete failure`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.deleteTemplate("wingspan.xml") } returns false

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.deleteTemplate("wingspan.xml")
        advanceUntilIdle()

        assertNull(viewModel.state.value.deleteConfirmation)
        assertEquals("Failed to delete template", viewModel.state.value.error)
        coVerify(exactly = 1) { storageManager.getAllTemplates() } // No reload on failure
    }

    @Test
    fun `deleteTemplate handles delete exception`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.deleteTemplate("wingspan.xml") } throws Exception("Delete error")

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.deleteTemplate("wingspan.xml")
        advanceUntilIdle()

        assertNull(viewModel.state.value.deleteConfirmation)
        assertTrue(viewModel.state.value.error?.contains("Error deleting template") == true)
        assertTrue(viewModel.state.value.error?.contains("Delete error") == true)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        coEvery { storageManager.getAllTemplates() } throws Exception("Error")

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `clearSelectedTemplate clears selected template`() = runTest {
        coEvery { storageManager.getAllTemplates() } returns listOf(testTemplateInfo1)
        coEvery { storageManager.loadTemplate("wingspan.xml") } returns testTemplate

        viewModel = TemplateBrowserViewModel(storageManager)
        advanceUntilIdle()

        viewModel.selectTemplate("wingspan.xml")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.selectedTemplate)

        viewModel.clearSelectedTemplate()

        assertNull(viewModel.state.value.selectedTemplate)
    }
}
