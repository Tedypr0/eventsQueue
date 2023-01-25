package org.example;

public class Event {
    private final String message;


    public Event(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        return prime + ((message == null) ? 0 : message.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        Event event = (Event) o;
        return (event.message.equals(this.message));
    }

}
