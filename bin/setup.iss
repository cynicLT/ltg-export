#ifndef AppVersion
  #define AppVersion "0.0.0"
#endif

#define AppName "LTG Export"
#define AppPublisher "CynicLT"
#define AppPublisherURL "https://github.com/cynicLT"
#define AppSupportURL "https://github.com/cynicLT"
#define AppUpdateURL "https://github.com/cynicLT"
#define AppExecName "ltg-export"
#define AppSource "."

[Setup]
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
AppId={{2168590F-7D4A-4960-BF50-CDF220DDE668}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppPublisherURL}
AppSupportURL={#AppSupportURL}
AppUpdatesURL={#AppUpdateURL}
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
PrivilegesRequired=admin
OutputBaseFilename={#AppExecName}
SetupIconFile="{#AppSource}\{#AppExecName}.ico"
UninstallDisplayIcon={uninstallexe}
;Password="Welcome1!"
;Encryption=yes

SolidCompression=yes
WizardStyle=modern
ChangesAssociations=yes
Uninstallable=True
CreateUninstallRegKey=True

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "{#AppSource}\app\*.*"; DestDir: "{app}\app"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#AppSource}\runtime\*.*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

Source: "{#AppSource}\*.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#AppSource}\*.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#AppSource}\*.ico"; DestDir: "{app}"; Flags: ignoreversion
#define ConfigFile "application.yml"

[Tasks]
Name: "desktopicon"; Description: "Create a Desktop shortcut"; GroupDescription: "Additional icons:"
Name: "startmenuicon"; Description: "Create a Start Menu shortcut"; GroupDescription: "Additional icons:"

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExecName}.exe"; Tasks: startmenuicon
Name: "{commondesktop}\{#AppName}"; Filename: "{app}\{#AppExecName}.exe"; Tasks: desktopicon
Name: "{group}\Uninstall {#AppName}"; Filename: "{uninstallexe}"

[Dirs]
Name: "{app}"; Permissions: everyone-modify

[UninstallDelete]
Type: filesandordirs; Name: "{group}\{#AppName}.lnk"
Type: filesandordirs; Name: "{commondesktop}\{#AppName}.lnk"