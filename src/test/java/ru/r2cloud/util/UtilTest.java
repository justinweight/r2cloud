package ru.r2cloud.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.aerse.mockfs.FailingByteChannelCallback;
import com.aerse.mockfs.MockFileSystem;

import ru.r2cloud.SampleClass;
import ru.r2cloud.TestUtil;

public class UtilTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testRotateImage() throws Exception {
		File rotatedImage = new File(tempFolder.getRoot(), UUID.randomUUID().toString() + ".jpg");
		TestUtil.copy("meteor.spectogram.jpg", rotatedImage);
		Util.rotateImage(rotatedImage);
		TestUtil.assertImage("rotated.meteor.spectogram.jpg", rotatedImage);
	}

	@Test
	public void testTotalSamples() throws Exception {
		long totalSamplesExpected = 50;
		assertEquals(totalSamplesExpected / 2, Util.readTotalSamples(setupGzippedFile(totalSamplesExpected)).longValue());
	}

	@Test
	public void testIOException() throws Exception {
		@SuppressWarnings("resource")
		MockFileSystem fs = new MockFileSystem(FileSystems.getDefault());
		Path file = fs.getPath(tempFolder.getRoot().getAbsolutePath()).resolve(UUID.randomUUID().toString());
		long totalSamplesExpected = 50;
		setupGzippedFile(totalSamplesExpected, file);
		fs.mock(file, new FailingByteChannelCallback(3));
		assertNull(Util.readTotalSamples(file));
	}

	@Test
	public void testUnknownFile() {
		assertNull(Util.readTotalSamples(tempFolder.getRoot().toPath().resolve(UUID.randomUUID().toString())));
	}

	@Test
	public void testSmallFile() throws Exception {
		// only 3 bytes
		File file = setupTempFile(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		assertNull(Util.readTotalSamples(file.toPath()));
	}

	@Test
	public void testUnsignedInt() throws Exception {
		File file = setupTempFile(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		assertEquals((4294967295L / 2), Util.readTotalSamples(file.toPath()).longValue());
	}

	@Test
	public void testDeleteDirectory() throws Exception {
		File firstLevel = new File(tempFolder.getRoot(), UUID.randomUUID().toString());
		File basedir = new File(firstLevel, UUID.randomUUID().toString());
		assertTrue(basedir.mkdirs());
		try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(basedir, UUID.randomUUID().toString())))) {
			w.append(UUID.randomUUID().toString());
		}
		Util.deleteDirectory(firstLevel.toPath());
		assertFalse(firstLevel.exists());
	}

	@Test
	public void testSplitComma() {
		List<String> result = Util.splitComma("test, , test2");
		assertEquals(2, result.size());
		assertEquals("test", result.get(0));
		assertEquals("test2", result.get(1));
	}

	@Test
	public void testSerializeJsonList() {
		assertEquals("{\"f1\":1,\"f10\":\"E2\",\"f11\":[1.1,2.2,3.3],\"f2\":2,\"f3\":3,\"f4\":4,\"f5\":5.1,\"f6\":6.1,\"f7\":\"f7\",\"f8\":[\"1\",\"2\",\"3\"],\"f9\":{\"f9\":[\"1\",\"2\",\"3\"]}}", Util.convertObject(new SampleClass()).toString());
	}

	private File setupTempFile(byte[] data) throws IOException, FileNotFoundException {
		File file = new File(tempFolder.getRoot(), UUID.randomUUID().toString());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data);
		}
		return file;
	}

	private Path setupGzippedFile(long totalSamplesExpected) throws IOException {
		Path result = tempFolder.getRoot().toPath().resolve(UUID.randomUUID().toString());
		setupGzippedFile(totalSamplesExpected, result);
		return result;
	}

	private static void setupGzippedFile(long totalSamplesExpected, Path result) throws IOException {
		try (OutputStream fos = new GZIPOutputStream(Files.newOutputStream(result))) {
			for (int i = 0; i < totalSamplesExpected; i++) {
				fos.write(0x01);
			}
		}
	}
}
