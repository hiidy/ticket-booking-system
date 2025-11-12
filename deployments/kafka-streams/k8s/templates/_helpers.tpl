{{- define "kafka-streams-system.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "kafka-streams-system.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
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
- name: SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG
  value: "org.apache.kafka.common.security.plain.PlainLoginModule required username='$(KAFKA_API_KEY)' password='$(KAFKA_API_SECRET)';"
- name: SPRING_KAFKA_PROPERTIES_BASIC_AUTH_CREDENTIALS_SOURCE
  value: "USER_INFO"
- name: SPRING_KAFKA_PROPERTIES_BASIC_AUTH_USER_INFO
  value: "$(SCHEMA_REGISTRY_API_KEY):$(SCHEMA_REGISTRY_API_SECRET)"
- name: CLIENT_ID
  value: "{{ .Release.Name }}-{{ .Chart.Name }}-client"
- name: HOST
  valueFrom:
    fieldRef:
      fieldPath: status.podIP
- name: SERVER_PORT
  value: "8080"
{{- end -}}