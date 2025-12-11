package si.fri.prpo.calendarservice.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
