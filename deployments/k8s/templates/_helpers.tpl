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