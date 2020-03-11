package br.com.iwe.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.LanguageCode;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.Voice;
import com.amazonaws.services.polly.model.VoiceId;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

public class S3PutHandler implements RequestHandler<S3Event, String> {

	private final AmazonPolly polly;
	private final Voice voice;
	private final AmazonS3 s3Client;
	private final String outputBucketMP3;
	private final AmazonSNS sns;
	private final String topicArn;

	public S3PutHandler() {

		s3Client = AmazonS3ClientBuilder.defaultClient();
		polly = AmazonPollyClientBuilder.defaultClient();

		voice = new Voice()
				.withLanguageCode(LanguageCode.fromValue(System.getenv("LANGUAGE_CODE")))
				.withId(VoiceId.fromValue(System.getenv("VOICE_ID")));

		outputBucketMP3 = System.getenv("OUTPUT_MP3_BUCKET");
		
		sns = AmazonSNSClientBuilder.defaultClient();
		topicArn = System.getenv("TOPIC_ARN");

	}

	@Override
	public String handleRequest(S3Event s3event, Context context) {

		S3EventNotificationRecord record = s3event.getRecords().get(0);

		String srcBucket = record.getS3().getBucket().getName();

		String srcKey = record.getS3().getObject().getUrlDecodedKey();

		// Download the image from S3 into a stream
		S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
		S3ObjectInputStream objectData = s3Object.getObjectContent();

		final String text = convert(objectData);

		// Synchronously ask Amazon Polly to describe available TTS voices.
		SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
				.withText(text)
				.withTextType(TextType.Ssml)
				.withVoiceId(voice.getId())
				.withOutputFormat(OutputFormat.Mp3);
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

		final InputStream audio = synthRes.getAudioStream();

		final String fileName = srcKey.substring(0,srcKey.indexOf('.')) + ".mp3";
		s3Client.putObject(outputBucketMP3, fileName, audio, null);
		
		final String msg = "Pronto! O audio " + fileName + " está disponível no Bucket!";
		final PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		sns.publish(publishRequest);

		return "Ok";
	}

	private String convert(S3ObjectInputStream objectData) {
		InputStreamReader isReader = new InputStreamReader(objectData);
		BufferedReader reader = new BufferedReader(isReader);
		StringBuffer sb = new StringBuffer();
		String str;
		try {
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
