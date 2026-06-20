// Single source of the Monaco SQL auto-completion provider, shared by every SQL
// editor (SqlLab, StudentDashboard, ...). Previously this ~40-line provider was
// copy-pasted into each editor.

export interface SchemaColumn { columnName: string; dataType: string; }
export interface SchemaTable { tableName: string; columns: SchemaColumn[]; }
export interface SchemaMetadata { tables: SchemaTable[]; }

/**
 * Build the Monaco completion provider for a given schema. `monaco` is passed in
 * (not imported) so this stays a pure function and is unit-testable with a fake.
 */
export function createSqlCompletionProvider(monaco: any, schemaMetadata: SchemaMetadata) {
  return {
    triggerCharacters: ['.', ' '],
    provideCompletionItems: (model: any, position: any) => {
      const word = model.getWordUntilPosition(position);
      const range = {
        startLineNumber: position.lineNumber,
        endLineNumber: position.lineNumber,
        startColumn: word.startColumn,
        endColumn: word.endColumn,
      };

      const suggestions: any[] = [];
      (schemaMetadata?.tables || []).forEach((table: any) => {
        suggestions.push({
          label: table.tableName,
          kind: monaco.languages.CompletionItemKind.Class,
          insertText: table.tableName,
          detail: 'Tabelle',
          range,
        });
        (table.columns || []).forEach((col: any) => {
          suggestions.push({
            label: col.columnName,
            kind: monaco.languages.CompletionItemKind.Field,
            insertText: col.columnName,
            detail: `${table.tableName} (${col.dataType})`,
            range,
          });
        });
      });
      return { suggestions };
    },
  };
}
