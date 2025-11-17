{{- define "booking-app.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{- define "booking-app.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "booking-app.mysql.fullname" -}}
{{ .Release.Name }}-mysql
{{- end -}}

{{- define "booking-app.redis.fullname" -}}
{{ .Release.Name }}-redis
{{- end -}}

{{- define "booking-app.fullname" -}}
{{ .Release.Name }}-booking-app
{{- end -}}
