package com.gpt.chatproject.utils;

import com.gpt.chatproject.enums.DallResponseType;
import com.gpt.chatproject.enums.DallSizeType;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageVariationRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DallUtils {

    @Autowired
    private GptUtils gptUtils;
    @Value("${openai.image_timeout}")
    private long IMAGE_TIME_OUT;

    public List<Image> dall2(File file, Integer N, DallSizeType sizeType, DallResponseType responseType) {
        OpenAiService service = new OpenAiService(gptUtils.initApi(IMAGE_TIME_OUT));
        CreateImageVariationRequest createImageVariationRequest = new CreateImageVariationRequest();
        createImageVariationRequest.setN(N);
        createImageVariationRequest.setSize(sizeType.getType());
        createImageVariationRequest.setResponseFormat(responseType.getType());
        ImageResult image = service.createImageVariation(createImageVariationRequest, file);
        return image.getData();
    }

    public List<Image> dall2(File file, Integer N, DallSizeType sizeType, DallResponseType responseType, String user) {
        OpenAiService service = new OpenAiService(gptUtils.initApi(IMAGE_TIME_OUT));
        CreateImageVariationRequest createImageVariationRequest = new CreateImageVariationRequest();
        createImageVariationRequest.setN(N);
        createImageVariationRequest.setSize(sizeType.getType());
        createImageVariationRequest.setResponseFormat(responseType.getType());
        createImageVariationRequest.setUser(user);
        ImageResult image = service.createImageVariation(createImageVariationRequest, file);
        return image.getData();
    }

    public List<Image> dall2(String prompt, Integer n, DallSizeType sizeType, DallResponseType responseType) {
        OpenAiService service = new OpenAiService(gptUtils.initApi(IMAGE_TIME_OUT));
        CreateImageRequest createImageRequest = new CreateImageRequest();
        createImageRequest.setPrompt(prompt);
        createImageRequest.setN(n);
        createImageRequest.setSize(sizeType.getType());
        createImageRequest.setResponseFormat(responseType.getType());
        ImageResult image = service.createImage(createImageRequest);
        return image.getData();
    }

    public List<Image> dall2(String prompt, Integer n, DallSizeType sizeType, DallResponseType responseType, String user) {
        OpenAiService service = new OpenAiService(gptUtils.initApi(IMAGE_TIME_OUT));
        CreateImageRequest createImageRequest = new CreateImageRequest();
        createImageRequest.setPrompt(prompt);
        createImageRequest.setN(n);
        createImageRequest.setSize(sizeType.getType());
        createImageRequest.setResponseFormat(responseType.getType());
        createImageRequest.setUser(user);
        ImageResult image = service.createImage(createImageRequest);
        return image.getData();
    }

}
