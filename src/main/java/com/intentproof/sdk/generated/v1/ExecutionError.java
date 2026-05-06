
package com.intentproof.sdk.generated.v1;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cause",
    "code",
    "message",
    "name",
    "stack"
})
@Generated("jsonschema2pojo")
public class ExecutionError {

    @JsonProperty("cause")
    private ExecutionError cause;
    /**
     * Optional stable machine-readable code.
     * 
     */
    @JsonProperty("code")
    @JsonPropertyDescription("Optional stable machine-readable code.")
    private String code;
    /**
     * Human-readable error message (may be empty string if the runtime provides none).
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Human-readable error message (may be empty string if the runtime provides none).")
    private String message;
    /**
     * Exception or error type name (e.g. Error, TypeError).
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Exception or error type name (e.g. Error, TypeError).")
    private String name;
    /**
     * Optional stringified stack trace; null when stacks are suppressed.
     * 
     */
    @JsonProperty("stack")
    @JsonPropertyDescription("Optional stringified stack trace; null when stacks are suppressed.")
    private String stack;

    @JsonProperty("cause")
    public ExecutionError getCause() {
        return cause;
    }

    @JsonProperty("cause")
    public void setCause(ExecutionError cause) {
        this.cause = cause;
    }

    /**
     * Optional stable machine-readable code.
     * 
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     * Optional stable machine-readable code.
     * 
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Human-readable error message (may be empty string if the runtime provides none).
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * Human-readable error message (may be empty string if the runtime provides none).
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Exception or error type name (e.g. Error, TypeError).
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Exception or error type name (e.g. Error, TypeError).
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Optional stringified stack trace; null when stacks are suppressed.
     * 
     */
    @JsonProperty("stack")
    public String getStack() {
        return stack;
    }

    /**
     * Optional stringified stack trace; null when stacks are suppressed.
     * 
     */
    @JsonProperty("stack")
    public void setStack(String stack) {
        this.stack = stack;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ExecutionError.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("cause");
        sb.append('=');
        sb.append(((this.cause == null)?"<null>":this.cause));
        sb.append(',');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null)?"<null>":this.code));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("stack");
        sb.append('=');
        sb.append(((this.stack == null)?"<null>":this.stack));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.cause == null)? 0 :this.cause.hashCode()));
        result = ((result* 31)+((this.stack == null)? 0 :this.stack.hashCode()));
        result = ((result* 31)+((this.code == null)? 0 :this.code.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExecutionError) == false) {
            return false;
        }
        ExecutionError rhs = ((ExecutionError) other);
        return ((((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.cause == rhs.cause)||((this.cause!= null)&&this.cause.equals(rhs.cause))))&&((this.stack == rhs.stack)||((this.stack!= null)&&this.stack.equals(rhs.stack))))&&((this.code == rhs.code)||((this.code!= null)&&this.code.equals(rhs.code))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))));
    }

}
