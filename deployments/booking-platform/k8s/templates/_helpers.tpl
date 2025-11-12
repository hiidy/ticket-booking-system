{{- define "booking-platform.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{- define "booking-platform.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "booking-platform.mysql.fullname" -}}
{{ .Release.Name }}-mysql
{{- end -}}

{{- define "booking-platform.redis.fullname" -}}
{{ .Release.Name }}-redis
{{- end -}}

{{- define "booking-platform.fullname" -}}
{{ .Release.Name }}-booking-platform
{{- end -}}
