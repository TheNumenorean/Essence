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
 * @author Francesco Macagno
 *
 */
public class AudioEncoder {
	
	private FFmpeg ffmpeg;
	private FFprobe ffprobe;


	public AudioEncoder(String ffmpegPath, String ffprobePath) throws IOException {
		this(new FFmpeg(ffmpegPath), new FFprobe(ffprobePath));
	}
	

	/**
	 * 
	 * @param ffmpeg
	 * @param ffprobe
	 */
	public AudioEncoder(FFmpeg ffmpeg, FFprobe ffprobe) {
		this.ffmpeg = ffmpeg;
		this.ffprobe = ffprobe;
	}
	
	public void convert(File from, File to) {
		
		//if(!from.isFile() || !to.isFile())
		//	throw new IllegalArgumentException("Inputs must be files!");
		
		
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
