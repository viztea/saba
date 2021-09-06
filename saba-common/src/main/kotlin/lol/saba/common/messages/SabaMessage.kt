package lol.saba.common.messages

interface SabaMessage {
    /** message that relates to a session */
    interface RTS : SabaMessage

    /** message from an actor. */
    interface FromActor : SabaMessage

    /** message to an actor. */
    interface ToActor : SabaMessage

    /** message from a director */
    interface FromDirector : SabaMessage

    /** message to a director */
    interface ToDirector : SabaMessage
}
