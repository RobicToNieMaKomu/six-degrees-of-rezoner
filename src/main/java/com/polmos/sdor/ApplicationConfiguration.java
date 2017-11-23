package com.polmos.sdor;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by RobicToNieMaKomu on 2017-11-11.
 */
@Configuration
public class ApplicationConfiguration {

    /*@Bean
    public AmazonSQS fake() {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:9324", ""));
        AmazonSQS sqs = builder.build();
        sqs.createQueue(State.CURR.getQueueName());
        sqs.createQueue(State.PREV.getQueueName());
        com.fasterxml.jackson.databind.node.ObjectNode node = new com.fasterxml.jackson.databind.node.ObjectNode(JsonNodeFactory.instance);
        node.put("login", "rezoner");
        node.put("id", 1);
        node.put("parent", "");

        sqs.sendMessage(new SendMessageRequest()
                .withQueueUrl(sqs.getQueueUrl(State.CURR.getQueueName()).getQueueUrl())
                .withMessageBody(node.toString()));
        return sqs;
    }*/

    @Bean
    public AmazonSQS get(@Value("${aws.key.id}") String accessKey,
                         @Value("${aws.key.secret}") String secretKey) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonSQSClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }
}
