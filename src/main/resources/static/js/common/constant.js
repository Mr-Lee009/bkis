const MESSAGE_LIST = {
    "MSG0001E": "システムエラーが発生しました。",
    "MSGV0016": "{0}は既に存在しています。",
    "MSGV0058": "対象を1件選択してください。",
    "MSGV0001": "{0}は必須入力です。",
    "MSGV0022": "ファイル拡張子が{0}と一致しません。",
    "MSGV0023": "ファイルサイズが{0}を超えています。",
    "MSGV0017": "{0}は存在しません。",
    "MSGV0045": "{0}に許容されていない文字が含まれています。",
    "MSGV0007": "{0}は{1}バイト以下で入力してください。",
    "MSGV0037": "フォルダー内にはpdfファイルとcsvファイルをそれぞれ1つずつ配置してください。フォルダー名：{0}",
    "MSGV0060": "zipファイルの展開が行えませんでした。ファイルの内容を確認してください。",
}

const HttpStatus = Object.freeze({
    OK: Symbol("OK"),
    INTERNAL_SERVER_ERROR: Symbol("INTERNAL_SERVER_ERROR"),
    CREATED: Symbol("CREATED"),
    BAD_REQUEST: Symbol("BAD_REQUEST"),
    CONTINUE: Symbol("CONTINUE"),
    CONFLICT: Symbol("CONFLICT"),
    NOT_FOUND: Symbol("NOT_FOUND"),
    PROCESSING: Symbol("PROCESSING"),
    UNAUTHORIZED: Symbol("UNAUTHORIZED"),
});

const FILE_TYPE = {
    MS: "MS",
    DL: "DL",
    MSPDF: "MSPDF",
    MSCSV: "MSCSV"
}

const FILE_EXTENSION = {
    PDF: "pdf",
    CSV: "csv",
    ZIP: "zip"
}

const SIZE_UNIT = {
    KB: "KB",
    MB: "MB",
}


const STEEL_TYPE = {
    REBAR: '1',
    FRAME: '2'
}

// Define company type constants
const CompanyTypes = Object.freeze([
    {code: "1", name: "建設会社"},                // CONSTRUCTION_COMPANY
    {code: "2", name: "ミルシート登録事業者"},      // MILLSHEET_REG_BUSINESS
    {code: "3", name: "業務運用"}                  // BUSINESS_OPERATION
]);

// Define email receive division constants
const EmailReceiveDivisions = Object.freeze([
    {code: "1", name: "受け取る"},    // RECEIVE
    {code: "0", name: "受け取らない"}  // DO_NOT_RECEIVE
]);



const FILE_NAME_MAX_LENGTH = 100; // in byte
