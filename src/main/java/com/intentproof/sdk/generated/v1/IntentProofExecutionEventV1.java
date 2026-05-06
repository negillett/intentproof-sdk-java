
package com.intentproof.sdk.generated.v1;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * IntentProof ExecutionEvent v1
 * <p>
 * Normative IntentProof source tree: https://github.com/IntentProof/intentproof-spec/tree/main/schema — $id is a logical URI; see README.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "action",
    "attributes",
    "completedAt",
    "correlationId",
    "durationMs",
    "error",
    "id",
    "inputs",
    "intent",
    "output",
    "startedAt",
    "status"
})
@Generated("jsonschema2pojo")
public class IntentProofExecutionEventV1 {

    /**
     * Stable identifier for the concrete operation. Many systems use hierarchical dotted names (e.g. vendor.resource.operation); REST paths, RPC names, or other conventions are allowed. The schema does not constrain the format.
     * (Required)
     * 
     */
    @JsonProperty("action")
    @JsonPropertyDescription("Stable identifier for the concrete operation. Many systems use hierarchical dotted names (e.g. vendor.resource.operation); REST paths, RPC names, or other conventions are allowed. The schema does not constrain the format.")
    private String action;
    /**
     * Flat primitive key/value metadata attached to the event.
     * 
     */
    @JsonProperty("attributes")
    @JsonPropertyDescription("Flat primitive key/value metadata attached to the event.")
    private Attributes attributes;
    /**
     * RFC 3339 / ISO 8601 instant when execution completed.
     * (Required)
     * 
     */
    @JsonProperty("completedAt")
    @JsonPropertyDescription("RFC 3339 / ISO 8601 instant when execution completed.")
    private String completedAt;
    /**
     * Optional cross-cutting identifier for distributed tracing. MUST be trimmed and non-empty when present.
     * 
     */
    @JsonProperty("correlationId")
    @JsonPropertyDescription("Optional cross-cutting identifier for distributed tracing. MUST be trimmed and non-empty when present.")
    private String correlationId;
    /**
     * Wall-clock duration in milliseconds between startedAt and completedAt.
     * (Required)
     * 
     */
    @JsonProperty("durationMs")
    @JsonPropertyDescription("Wall-clock duration in milliseconds between startedAt and completedAt.")
    private Double durationMs;
    @JsonProperty("error")
    private ExecutionError error;
    /**
     * Stable unique identifier for this execution record.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Stable unique identifier for this execution record.")
    private String id;
    /**
     * Captured call inputs. Values MUST be JSON-serializable (see semantics/serialization_rules.md).
     * (Required)
     * 
     */
    @JsonProperty("inputs")
    @JsonPropertyDescription("Captured call inputs. Values MUST be JSON-serializable (see semantics/serialization_rules.md).")
    private Inputs inputs;
    /**
     * Natural-language description of what the user or caller was trying to achieve (often a full sentence or question). No fixed grammar; SHOULD stay human-readable in logs and UIs.
     * (Required)
     * 
     */
    @JsonProperty("intent")
    @JsonPropertyDescription("Natural-language description of what the user or caller was trying to achieve (often a full sentence or question). No fixed grammar; SHOULD stay human-readable in logs and UIs.")
    private String intent;
    @JsonProperty("output")
    private Object output;
    /**
     * RFC 3339 / ISO 8601 instant when execution started.
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("RFC 3339 / ISO 8601 instant when execution started.")
    private String startedAt;
    /**
     * Terminal execution status.
     * (Required)
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("Terminal execution status.")
    private IntentProofExecutionEventV1.Status status;

    /**
     * Stable identifier for the concrete operation. Many systems use hierarchical dotted names (e.g. vendor.resource.operation); REST paths, RPC names, or other conventions are allowed. The schema does not constrain the format.
     * (Required)
     * 
     */
    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    /**
     * Stable identifier for the concrete operation. Many systems use hierarchical dotted names (e.g. vendor.resource.operation); REST paths, RPC names, or other conventions are allowed. The schema does not constrain the format.
     * (Required)
     * 
     */
    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Flat primitive key/value metadata attached to the event.
     * 
     */
    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Flat primitive key/value metadata attached to the event.
     * 
     */
    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * RFC 3339 / ISO 8601 instant when execution completed.
     * (Required)
     * 
     */
    @JsonProperty("completedAt")
    public String getCompletedAt() {
        return completedAt;
    }

    /**
     * RFC 3339 / ISO 8601 instant when execution completed.
     * (Required)
     * 
     */
    @JsonProperty("completedAt")
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Optional cross-cutting identifier for distributed tracing. MUST be trimmed and non-empty when present.
     * 
     */
    @JsonProperty("correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Optional cross-cutting identifier for distributed tracing. MUST be trimmed and non-empty when present.
     * 
     */
    @JsonProperty("correlationId")
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Wall-clock duration in milliseconds between startedAt and completedAt.
     * (Required)
     * 
     */
    @JsonProperty("durationMs")
    public Double getDurationMs() {
        return durationMs;
    }

    /**
     * Wall-clock duration in milliseconds between startedAt and completedAt.
     * (Required)
     * 
     */
    @JsonProperty("durationMs")
    public void setDurationMs(Double durationMs) {
        this.durationMs = durationMs;
    }

    @JsonProperty("error")
    public ExecutionError getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(ExecutionError error) {
        this.error = error;
    }

    /**
     * Stable unique identifier for this execution record.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Stable unique identifier for this execution record.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Captured call inputs. Values MUST be JSON-serializable (see semantics/serialization_rules.md).
     * (Required)
     * 
     */
    @JsonProperty("inputs")
    public Inputs getInputs() {
        return inputs;
    }

    /**
     * Captured call inputs. Values MUST be JSON-serializable (see semantics/serialization_rules.md).
     * (Required)
     * 
     */
    @JsonProperty("inputs")
    public void setInputs(Inputs inputs) {
        this.inputs = inputs;
    }

    /**
     * Natural-language description of what the user or caller was trying to achieve (often a full sentence or question). No fixed grammar; SHOULD stay human-readable in logs and UIs.
     * (Required)
     * 
     */
    @JsonProperty("intent")
    public String getIntent() {
        return intent;
    }

    /**
     * Natural-language description of what the user or caller was trying to achieve (often a full sentence or question). No fixed grammar; SHOULD stay human-readable in logs and UIs.
     * (Required)
     * 
     */
    @JsonProperty("intent")
    public void setIntent(String intent) {
        this.intent = intent;
    }

    @JsonProperty("output")
    public Object getOutput() {
        return output;
    }

    @JsonProperty("output")
    public void setOutput(Object output) {
        this.output = output;
    }

    /**
     * RFC 3339 / ISO 8601 instant when execution started.
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    public String getStartedAt() {
        return startedAt;
    }

    /**
     * RFC 3339 / ISO 8601 instant when execution started.
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * Terminal execution status.
     * (Required)
     * 
     */
    @JsonProperty("status")
    public IntentProofExecutionEventV1.Status getStatus() {
        return status;
    }

    /**
     * Terminal execution status.
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(IntentProofExecutionEventV1.Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(IntentProofExecutionEventV1 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("action");
        sb.append('=');
        sb.append(((this.action == null)?"<null>":this.action));
        sb.append(',');
        sb.append("attributes");
        sb.append('=');
        sb.append(((this.attributes == null)?"<null>":this.attributes));
        sb.append(',');
        sb.append("completedAt");
        sb.append('=');
        sb.append(((this.completedAt == null)?"<null>":this.completedAt));
        sb.append(',');
        sb.append("correlationId");
        sb.append('=');
        sb.append(((this.correlationId == null)?"<null>":this.correlationId));
        sb.append(',');
        sb.append("durationMs");
        sb.append('=');
        sb.append(((this.durationMs == null)?"<null>":this.durationMs));
        sb.append(',');
        sb.append("error");
        sb.append('=');
        sb.append(((this.error == null)?"<null>":this.error));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("inputs");
        sb.append('=');
        sb.append(((this.inputs == null)?"<null>":this.inputs));
        sb.append(',');
        sb.append("intent");
        sb.append('=');
        sb.append(((this.intent == null)?"<null>":this.intent));
        sb.append(',');
        sb.append("output");
        sb.append('=');
        sb.append(((this.output == null)?"<null>":this.output));
        sb.append(',');
        sb.append("startedAt");
        sb.append('=');
        sb.append(((this.startedAt == null)?"<null>":this.startedAt));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
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
        result = ((result* 31)+((this.output == null)? 0 :this.output.hashCode()));
        result = ((result* 31)+((this.completedAt == null)? 0 :this.completedAt.hashCode()));
        result = ((result* 31)+((this.inputs == null)? 0 :this.inputs.hashCode()));
        result = ((result* 31)+((this.action == null)? 0 :this.action.hashCode()));
        result = ((result* 31)+((this.startedAt == null)? 0 :this.startedAt.hashCode()));
        result = ((result* 31)+((this.attributes == null)? 0 :this.attributes.hashCode()));
        result = ((result* 31)+((this.correlationId == null)? 0 :this.correlationId.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.error == null)? 0 :this.error.hashCode()));
        result = ((result* 31)+((this.durationMs == null)? 0 :this.durationMs.hashCode()));
        result = ((result* 31)+((this.intent == null)? 0 :this.intent.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IntentProofExecutionEventV1) == false) {
            return false;
        }
        IntentProofExecutionEventV1 rhs = ((IntentProofExecutionEventV1) other);
        return (((((((((((((this.output == rhs.output)||((this.output!= null)&&this.output.equals(rhs.output)))&&((this.completedAt == rhs.completedAt)||((this.completedAt!= null)&&this.completedAt.equals(rhs.completedAt))))&&((this.inputs == rhs.inputs)||((this.inputs!= null)&&this.inputs.equals(rhs.inputs))))&&((this.action == rhs.action)||((this.action!= null)&&this.action.equals(rhs.action))))&&((this.startedAt == rhs.startedAt)||((this.startedAt!= null)&&this.startedAt.equals(rhs.startedAt))))&&((this.attributes == rhs.attributes)||((this.attributes!= null)&&this.attributes.equals(rhs.attributes))))&&((this.correlationId == rhs.correlationId)||((this.correlationId!= null)&&this.correlationId.equals(rhs.correlationId))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.error == rhs.error)||((this.error!= null)&&this.error.equals(rhs.error))))&&((this.durationMs == rhs.durationMs)||((this.durationMs!= null)&&this.durationMs.equals(rhs.durationMs))))&&((this.intent == rhs.intent)||((this.intent!= null)&&this.intent.equals(rhs.intent))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }


    /**
     * Terminal execution status.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Status {

        OK("ok"),
        ERROR("error");
        private final String value;
        private final static Map<String, IntentProofExecutionEventV1.Status> CONSTANTS = new HashMap<String, IntentProofExecutionEventV1.Status>();

        static {
            for (IntentProofExecutionEventV1.Status c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static IntentProofExecutionEventV1.Status fromValue(String value) {
            IntentProofExecutionEventV1.Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
