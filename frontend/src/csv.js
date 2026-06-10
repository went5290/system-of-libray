function csvCell(value) {
  if (value === null || value === undefined) return ''

  let text = String(value)
  if (/^[=+\-@]/.test(text)) {
    text = `'${text}`
  }
  return `"${text.replaceAll('"', '""')}"`
}

export function buildCsv(columns, rows) {
  const header = columns.map(column => csvCell(column.label)).join(',')
  const body = rows.map(row => columns.map(column => csvCell(column.value(row))).join(','))
  return `\uFEFF${[header, ...body].join('\r\n')}`
}

export function downloadCsv(filename, columns, rows) {
  const blob = new Blob([buildCsv(columns, rows)], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
