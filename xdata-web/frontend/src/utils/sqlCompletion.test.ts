import { createSqlCompletionProvider, SchemaMetadata } from './sqlCompletion';

const monaco = {
  languages: { CompletionItemKind: { Class: 7, Field: 3 } },
};

const model = {
  getWordUntilPosition: () => ({ startColumn: 1, endColumn: 3 }),
};
const position = { lineNumber: 1 };

const schema: SchemaMetadata = {
  tables: [
    { tableName: 'users', columns: [
      { columnName: 'id', dataType: 'INT' },
      { columnName: 'name', dataType: 'VARCHAR' },
    ] },
  ],
};

test('suggests every table and column from the schema', () => {
  const provider = createSqlCompletionProvider(monaco, schema);
  const { suggestions } = provider.provideCompletionItems(model, position);

  expect(suggestions).toHaveLength(3); // 1 table + 2 columns
  const labels = suggestions.map((s: any) => s.label);
  expect(labels).toEqual(['users', 'id', 'name']);
});

test('tags tables as Class and columns as Field, with detail', () => {
  const provider = createSqlCompletionProvider(monaco, schema);
  const { suggestions } = provider.provideCompletionItems(model, position);

  const table = suggestions.find((s: any) => s.label === 'users');
  const column = suggestions.find((s: any) => s.label === 'id');
  expect(table.kind).toBe(monaco.languages.CompletionItemKind.Class);
  expect(table.detail).toBe('Tabelle');
  expect(column.kind).toBe(monaco.languages.CompletionItemKind.Field);
  expect(column.detail).toBe('users (INT)');
});

test('sets the replacement range from the word under the cursor', () => {
  const provider = createSqlCompletionProvider(monaco, schema);
  const { suggestions } = provider.provideCompletionItems(model, position);
  expect(suggestions[0].range).toEqual({
    startLineNumber: 1, endLineNumber: 1, startColumn: 1, endColumn: 3,
  });
});

test('returns no suggestions for an empty schema', () => {
  const provider = createSqlCompletionProvider(monaco, { tables: [] });
  expect(provider.provideCompletionItems(model, position).suggestions).toHaveLength(0);
});
