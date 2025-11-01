{{/*
Define common labels
*/}}
{{- define "kafka-streams-system.labels" -}}
helm.sh/chart: {{ include "kafka-streams-system.chart" . }}
{{ include "kafka-streams-system.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "kafka-streams-system.selectorLabels" -}}
app.kubernetes.io/name: {{ include "kafka-streams-system.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Create chart name
*/}}
{{- define "kafka-streams-system.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default name
*/}}
{{- define "kafka-streams-system.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "kafka-streams-system.kafkaEnv" -}}
- name: KAFKA_BOOTSTRAP_SERVERS
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-kafka-config
      key: KAFKA_BOOTSTRAP_SERVERS
- name: SCHEMA_REGISTRY_URL
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-kafka-config
      key: SCHEMA_REGISTRY_URL
- name: KAFKA_TOPIC_PREFIX
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-kafka-config
      key: KAFKA_TOPIC_PREFIX
- name: KAFKA_SECURITY_PROTOCOL
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-kafka-config
      key: KAFKA_SECURITY_PROTOCOL
- name: KAFKA_SASL_MECHANISM
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-kafka-config
      key: KAFKA_SASL_MECHANISM
- name: KAFKA_API_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-kafka-auth
      key: KAFKA_API_KEY
- name: KAFKA_API_SECRET
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-kafka-auth
      key: KAFKA_API_SECRET
- name: SCHEMA_REGISTRY_API_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-kafka-auth
      key: SCHEMA_REGISTRY_API_KEY
- name: SCHEMA_REGISTRY_API_SECRET
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-kafka-auth
      key: SCHEMA_REGISTRY_API_SECRET
- name: SPRING_PROFILES_ACTIVE
  value: "prod"
{{- end -}}