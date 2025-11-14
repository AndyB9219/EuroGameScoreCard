package com.eurogame.scorecard.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotUtilsTest {

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var mockImageBitmap: ImageBitmap
    private lateinit var mockAndroidBitmap: Bitmap
    private lateinit var mockOutputStream: OutputStream
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        mockImageBitmap = mockk(relaxed = true)
        mockAndroidBitmap = mockk(relaxed = true)
        mockOutputStream = mockk(relaxed = true)
        mockUri = mockk(relaxed = true)

        every { mockContext.contentResolver } returns mockContentResolver
        every { mockImageBitmap.asAndroidBitmap() } returns mockAndroidBitmap
        every { mockAndroidBitmap.width } returns 1080
        every { mockAndroidBitmap.height } returns 1920
        every { mockAndroidBitmap.compress(any(), any(), any()) } returns true
        every { mockOutputStream.close() } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveImageToGallery returns success when image is saved successfully`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test_scorecard"
        )

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("test_scorecard") == true)
        assertTrue(result.getOrNull()?.contains(".png") == true)
    }

    @Test
    fun `saveImageToGallery inserts into MediaStore with correct values`() = runTest {
        val capturedContentValues = slot<ContentValues>()
        every {
            mockContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, capture(capturedContentValues))
        } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "wingspan"
        )

        val values = capturedContentValues.captured
        assertTrue(values.getAsString(MediaStore.Images.Media.DISPLAY_NAME).startsWith("wingspan_"))
        assertEquals("image/png", values.getAsString(MediaStore.Images.Media.MIME_TYPE))
        assertEquals(1080, values.getAsInteger(MediaStore.Images.Media.WIDTH))
        assertEquals(1920, values.getAsInteger(MediaStore.Images.Media.HEIGHT))
    }

    @Test
    fun `saveImageToGallery compresses bitmap to PNG with 100 quality`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        verify {
            mockAndroidBitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                mockOutputStream
            )
        }
    }

    @Test
    fun `saveImageToGallery closes output stream after writing`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        verify { mockOutputStream.close() }
    }

    @Test
    fun `saveImageToGallery returns failure when MediaStore insert fails`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns null

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Failed to create MediaStore entry", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveImageToGallery returns failure when output stream is null`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns null

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Failed to open output stream", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveImageToGallery returns failure when bitmap compress throws exception`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream
        every { mockAndroidBitmap.compress(any(), any(), any()) } throws IOException("Disk full")

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Disk full", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveImageToGallery handles unexpected exceptions`() = runTest {
        every { mockContentResolver.insert(any(), any()) } throws RuntimeException("Unexpected error")

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Unexpected error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveImageToGallery uses default filename when basename is empty`() = runTest {
        val capturedContentValues = slot<ContentValues>()
        every {
            mockContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, capture(capturedContentValues))
        } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            ""
        )

        val displayName = capturedContentValues.captured.getAsString(MediaStore.Images.Media.DISPLAY_NAME)
        // Should still create a filename with timestamp, just no prefix
        assertTrue(displayName.endsWith(".png"))
        assertTrue(displayName.contains("_")) // timestamp separator
    }

    @Test
    fun `saveImageToGallery generates timestamped filename`() = runTest {
        val capturedContentValues = slot<ContentValues>()
        every {
            mockContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, capture(capturedContentValues))
        } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "wingspan"
        )

        val displayName = capturedContentValues.captured.getAsString(MediaStore.Images.Media.DISPLAY_NAME)
        // Format: wingspan_yyyyMMdd_HHmmss.png
        assertTrue(displayName.matches(Regex("wingspan_\\d{8}_\\d{6}\\.png")))
    }

    @Test
    fun `saveImageToGallery includes relative path for Android Q and above`() = runTest {
        val capturedContentValues = slot<ContentValues>()
        every {
            mockContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, capture(capturedContentValues))
        } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        val values = capturedContentValues.captured
        // On Android Q+ (SDK 29+), relative path should be set
        // Note: This test assumes we're testing on SDK >= 29 environment
        // In real testing, this might need SDK version checking
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            assertEquals("Pictures/EuroGameScorecard", values.getAsString(MediaStore.Images.Media.RELATIVE_PATH))
        }
    }

    @Test
    fun `saveImageToGallery success message includes full path`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        val result = ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "wingspan"
        )

        assertTrue(result.isSuccess)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message!!.contains("Scorecard saved to Pictures/EuroGameScorecard/"))
        assertTrue(message.contains("wingspan_"))
        assertTrue(message.endsWith(".png"))
    }

    @Test
    fun `saveImageToGallery converts ImageBitmap to Android Bitmap`() = runTest {
        every { mockContentResolver.insert(any(), any()) } returns mockUri
        every { mockContentResolver.openOutputStream(mockUri) } returns mockOutputStream

        ScreenshotUtils.saveImageToGallery(
            mockContext,
            mockImageBitmap,
            "test"
        )

        verify { mockImageBitmap.asAndroidBitmap() }
    }
}
