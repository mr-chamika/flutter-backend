package com.example.prChat.controller;


import com.example.prChat.model.Chat;
import com.example.prChat.model.Message;
import com.example.prChat.model.dto.Messagedto;
import com.example.prChat.repo.ChatRepo;
import com.example.prChat.repo.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ChatRepo chatRepo;

    @PostMapping("/create")
    public Message create(@RequestBody Messagedto message) {

        Message x = new Message();

        Chat chat = chatRepo.findById(message.getChatId()).get();

        x.setChatId(message.getChatId());
        x.setText(message.getText());
        x.setSenderId(message.getSenderId());

        x = messageRepo.save(x);

        chat.setLastMessageId(x.get_id());

        chatRepo.save(chat);

        // Broadcast to all subscribers of this chat
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), x);

        return x;

    }
    @GetMapping("/get")
    public List<Message> get(@RequestParam String id) {

        return messageRepo.findByChatId(id);

    }

    @GetMapping("/one")
    public Message getOne(@RequestParam String id) {

        return messageRepo.findById(id).get();

    }


    @PutMapping("/delete")
    public ResponseEntity<Map<String, String>> delete(@RequestParam String id) {
        Optional<Message> msg = messageRepo.findById(id);
        if (msg.isPresent()) {
            messageRepo.delete(msg.get());
            return ResponseEntity.ok(Map.of("message", "Message deleted"));
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Message not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


//
//
//@GetMapping("/list")
//public ResponseEntity<?> getChatList(@RequestParam String id) {
//
//        List<Chat> list = chatRepo.findByParticipantsContaining(id);
//
//    if (!list.isEmpty()) {
//
//        return ResponseEntity.ok(list);
//
//        }
//
//        return ResponseEntity.badRequest().body("Chat not found");
//
//    }

}
