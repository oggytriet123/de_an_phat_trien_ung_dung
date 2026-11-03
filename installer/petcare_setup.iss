; Script Inno Setup 6 cho phan mem PetCare Management System
; Tu dong tao bo cai dat chuyen nghiep voi Embedded JRE, Desktop Shortcut va Uninstaller sach se

#define MyAppName "PetCare"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "PetCare Clinic Solutions"
#define MyAppURL "https://github.com/PetCare"
#define MyAppExeName "PetCare.exe"
#define MyAppAssocName MyAppName + " Application"
#define AppSourcePath "..\dist\PetCare"
#define AppIconPath "..\assets\app_icon.ico"

[Setup]
; Thong tin dinh danh duy nhat cho ung dung
AppId={{5F8A2D39-6C18-4E87-9A1B-E8972D56F144}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} v{#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

; Thu muc cai dat mac dinh (C:\Program Files\PetCare hoac tuy chon cua nguoi dung)
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=no

; Giao dien hien dai, ho tro gỡ cài đặt sạch sẽ
WizardStyle=modern
SetupIconFile={#AppIconPath}
UninstallDisplayIcon={app}\{#MyAppExeName}
UninstallDisplayName={#MyAppName} Management System

; Nen du lieu toi uu de file setup nho gon
Compression=lzma2/ultra64
SolidCompression=yes

; Quyen cai dat: Ho tro ca Admin lan User thuong
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog

; Thu muc xuat file setup
OutputDir=Output
OutputBaseFilename=PetCare_Setup_v{#MyAppVersion}

; Khong bat buoc khoi dong lai may
RestartIfNeededByRun=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
Name: "startmenuicon"; Description: "Create Start Menu shortcut"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
; Dong goi toan bo thu muc ung dung da nhung JRE tu dist\PetCare
Source: "{#AppSourcePath}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; Icon ung dung
Source: "{#AppIconPath}"; DestDir: "{app}\assets"; Flags: ignoreversion

[Icons]
; Phim tat Start Menu
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"
Name: "{group}\Uninstall {#MyAppName}"; Filename: "{uninstallexe}"
; Phim tat ngoai Desktop (neu nguoi dung tich chon)
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; Tuy chon khoi chay ung dung ngay sau khi cai dat thanh cong
Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Filename: "{app}\{#MyAppExeName}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
; Xoa triet de cac file cache hoac log tao ra trong qua trinh su dung
Type: filesandordirs; Name: "{app}\runtime"
Type: filesandordirs; Name: "{app}\app"
Type: filesandordirs; Name: "{app}\assets"
Type: filesandordirs; Name: "{app}"
