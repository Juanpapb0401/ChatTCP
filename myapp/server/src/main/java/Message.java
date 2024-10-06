import java.time.LocalDateTime;

class Message {
    private String sender;
    private String recipient;
    private String content;
    private String messageType; // "text" o "voice"
    private LocalDateTime timestamp;

    public Message(String sender, String recipient, String content, String messageType) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return timestamp + " [" + messageType + "] " + sender + " to " + recipient + ": " + content;
    }
}