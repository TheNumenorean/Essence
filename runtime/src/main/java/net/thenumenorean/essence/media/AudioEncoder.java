/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;
import java.io.IOException;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;

/**
 * This class encodes tracks into mp3's using ffmpeg.
 * 
 * @author Francesco Macagno
 *
 */
public class AudioEncoder {
	
	private FFmpeg ffmpeg;
	private FFprobe ffprobe;

	/**
	 * Creates an encoder using the given paths.
	 * @param ffmpegPath The path to ffmpeg
	 * @param ffprobePath The path to ffprobe
	 * @throws IOException If there is an issue reading the passed files
	 */
	public AudioEncoder(String ffmpegPath, String ffprobePath) throws IOException {
		this(new FFmpeg(ffmpegPath), new FFprobe(ffprobePath));
	}
	

	/**
	 * Creates an encoder using the given files.
	 * @param ffmpeg The ffmpeg folder
	 * @param ffprobe The ffprobe folder
	 */
	public AudioEncoder(FFmpeg ffmpeg, FFprobe ffprobe) {
		this.ffmpeg = ffmpeg;
		this.ffprobe = ffprobe;
	}
	
	/**
	 * Converts a file to mp3 and saves it into the given location, overwriting it if neccessary.
	 * @param from The file to convert
	 * @param to Where to save the file to
	 */
	public void convert(File from, File to) {
		
		FFmpegBuilder b= new FFmpegBuilder();
		b.addInput(from.getAbsolutePath());
		b.overrideOutputFiles(true);
		
		FFmpegOutputBuilder output = b.addOutput(to.getAbsolutePath());
		output.setFormat("mp3");
		output.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL);
		output.done();
		
		FFmpegExecutor exec = new FFmpegExecutor(ffmpeg, ffprobe);
		exec.createJob(b).run();
		
	}

}
