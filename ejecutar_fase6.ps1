# FASE 6 - Pruebas manuales de endpoints contra MariaDB real
# Ejecutar desde la raiz del repo (admin-proyectos-main) con la app corriendo en el puerto 8081.
# Requiere la variable de entorno SPRING_DATASOURCE_PASSWORD (la contrasena NO se guarda en ningun archivo).

$mysql = "C:\Program Files\MariaDB 11.4\bin\mysql.exe"
$base  = "http://localhost:8081"

if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    Write-Host "Define primero: `$env:SPRING_DATASOURCE_PASSWORD = 'tu_contrasena'"
    return
}

New-Item -ItemType Directory -Force -Path "evidencias\endpoints", "evidencias\bd" | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding $false

function Sql([string]$query) {
    $r = & $mysql -u root "-p$($env:SPRING_DATASOURCE_PASSWORD)" -t -e $query dbgestor 2>$null | Out-String
    if ([string]::IsNullOrWhiteSpace($r)) { return "(sin registros)" }
    return $r.TrimEnd()
}

function SqlValor([string]$query) {
    $r = & $mysql -u root "-p$($env:SPRING_DATASOURCE_PASSWORD)" -N -B -e $query dbgestor 2>$null | Select-Object -First 1
    return "$r".Trim()
}

function Guardar-BD([string]$id, [string]$antes, [string]$despues) {
    $txt = "ID: $id`r`n--- ANTES ---`r`n$antes`r`n--- DESPUES ---`r`n$despues`r`n"
    [IO.File]::WriteAllText((Join-Path (Get-Location) "evidencias\bd\$id.txt"), $txt, $utf8)
    Write-Host "BD ($id)"
    Write-Host $txt
}

function Llamar([string]$id, [string]$metodo, [string]$ruta, [string]$cuerpo, [int]$esperado) {
    $salida = [IO.Path]::GetTempFileName()
    $curlArgs = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", $metodo, "$base$ruta")
    if ($cuerpo) {
        $bf = [IO.Path]::GetTempFileName()
        [IO.File]::WriteAllText($bf, $cuerpo, $utf8)
        $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$bf")
    }
    $codigo = & curl.exe @curlArgs
    $resp = [IO.File]::ReadAllText($salida, $utf8)
    $veredicto = if ([int]$codigo -eq $esperado) { "COINCIDE" } else { "DIFIERE" }
    $txt = "ID: $id`r`nPeticion: $metodo $ruta`r`nCuerpo enviado: $cuerpo`r`nCodigo HTTP esperado: $esperado`r`nCodigo HTTP obtenido: $codigo`r`nResultado: $veredicto`r`nRespuesta: $resp`r`n"
    [IO.File]::WriteAllText((Join-Path (Get-Location) "evidencias\endpoints\$id.txt"), $txt, $utf8)
    Write-Host $txt
    Write-Host "-----------------------------------------"
}

# Comprobar que la app responde
$ping = & curl.exe -s -o NUL -w "%{http_code}" -X POST "$base/buscarUsuarios"
if ($ping -ne "200") {
    Write-Host "La app no responde en $base (codigo $ping). Levantala primero con: .\mvnw.cmd spring-boot:run"
    return
}

$correo = "prueba.p1@example.com"
$qUsuario = "SELECT id_usuario, nombre, correo_electronico, rol FROM usuario WHERE correo_electronico='$correo';"

# Limpieza previa: solo datos de prueba, nunca los registros del dump
Sql "DELETE FROM usuario WHERE correo_electronico='$correo'; DELETE FROM tareas WHERE nombre='Tarea Prueba P1'; DELETE FROM proyectos WHERE nombre='Proyecto Prueba P1';" | Out-Null

# ---------- EP-01 POST /addUsuario valido ----------
$jsonUsuario = @"
{"nombre":"Usuario Prueba P1","correo_electronico":"$correo","contrasena":"prueba123","rol":"Estudiante","imagen_usuario":"#FFFFFF"}
"@
$antes = Sql $qUsuario
Llamar "EP-01_POST_addUsuario" "POST" "/addUsuario" $jsonUsuario 200
$despues = Sql $qUsuario
Guardar-BD "EP-01_POST_addUsuario" $antes $despues
$idU = SqlValor "SELECT id_usuario FROM usuario WHERE correo_electronico='$correo';"

# ---------- EP-02 GET /buscarUsuario ----------
Llamar "EP-02_GET_buscarUsuario" "GET" "/buscarUsuario?id=$idU" "" 200

# ---------- EP-03 / EP-04 login ----------
Llamar "EP-03_POST_getUsuarioLogin_correcto" "POST" "/getUsuarioLogin" "{`"correo_electronico`":`"$correo`",`"contrasena`":`"prueba123`"}" 200
Llamar "EP-04_POST_getUsuarioLogin_incorrecto" "POST" "/getUsuarioLogin" "{`"correo_electronico`":`"$correo`",`"contrasena`":`"incorrecta`"}" 200

# ---------- EP-05 POST /addProyecto ----------
$qProyecto = "SELECT id_proyecto, nombre, estado_proyecto FROM proyectos WHERE nombre='Proyecto Prueba P1';"
$jsonProyecto = @"
{"nombre":"Proyecto Prueba P1","descripcion":"Proyecto de prueba P1","fecha_inicio":"2026-10-01","fecha_finalizacion":"2026-10-31","estado_proyecto":"En proceso","fondo":"#FFFFFF"}
"@
$antes = Sql $qProyecto
Llamar "EP-05_POST_addProyecto" "POST" "/addProyecto" $jsonProyecto 200
$despues = Sql $qProyecto
Guardar-BD "EP-05_POST_addProyecto" $antes $despues
$idP = SqlValor "SELECT id_proyecto FROM proyectos WHERE nombre='Proyecto Prueba P1';"

# ---------- EP-06 GET /buscarProyecto ----------
Llamar "EP-06_GET_buscarProyecto" "GET" "/buscarProyecto?id_proyecto=$idP" "" 200

# ---------- EP-07 POST /addTareas ----------
$qTarea = "SELECT id_tarea, nombre, id_proyecto FROM tareas WHERE nombre='Tarea Prueba P1';"
$jsonTarea = @"
{"nombre":"Tarea Prueba P1","descripcion":"Tarea de prueba P1","fecha_vencimiento":"2026-10-15","lista":"pendiente","estado":"danger","id_proyecto":$idP}
"@
$antes = Sql $qTarea
Llamar "EP-07_POST_addTareas" "POST" "/addTareas" $jsonTarea 200
$despues = Sql $qTarea
Guardar-BD "EP-07_POST_addTareas" $antes $despues

# ---------- EP-08 POST /getTarea ----------
Llamar "EP-08_POST_getTarea" "POST" "/getTarea" "{`"id_proyecto`":$idP}" 200

# ---------- EP-09 dato invalido: correo y contrasena vacios (esperado 400) ----------
$qInvalido = "SELECT id_usuario, nombre FROM usuario WHERE nombre='Sin Correo P1';"
$jsonInvalido = '{"nombre":"Sin Correo P1","correo_electronico":"","contrasena":"","rol":"Estudiante","imagen_usuario":"#000000"}'
$antes = Sql $qInvalido
Llamar "EP-09_POST_addUsuario_datos_vacios" "POST" "/addUsuario" $jsonInvalido 400
$despues = Sql $qInvalido
Guardar-BD "EP-09_POST_addUsuario_datos_vacios" $antes $despues

# ---------- EP-10 ID inexistente (esperado 404) ----------
Llamar "EP-10_GET_buscarUsuario_inexistente" "GET" "/buscarUsuario?id=99999" "" 404

# ---------- EP-11 JSON mal formado (esperado 400) ----------
Llamar "EP-11_POST_addUsuario_json_malformado" "POST" "/addUsuario" "{esto no es json" 400

# ---------- EP-12 DELETE /deleteUsuario ----------
$jsonDelete = "{`"id_usuario`":$idU,`"nombre`":`"Usuario Prueba P1`",`"correo_electronico`":`"$correo`",`"contrasena`":`"prueba123`",`"rol`":`"Estudiante`",`"imagen_usuario`":`"#FFFFFF`"}"
$antes = Sql $qUsuario
Llamar "EP-12_DELETE_deleteUsuario" "DELETE" "/deleteUsuario" $jsonDelete 200
$despues = Sql $qUsuario
Guardar-BD "EP-12_DELETE_deleteUsuario" $antes $despues

# ---------- Limpieza y verificacion final ----------
Sql "DELETE FROM tareas WHERE nombre='Tarea Prueba P1'; DELETE FROM proyectos WHERE nombre='Proyecto Prueba P1'; DELETE FROM usuario WHERE correo_electronico='$correo' OR nombre='Sin Correo P1';" | Out-Null
Write-Host "=== Verificacion final: debe quedar 3 usuarios y 1 proyecto ==="
Sql "SELECT (SELECT COUNT(*) FROM usuario) AS usuarios, (SELECT COUNT(*) FROM proyectos) AS proyectos, (SELECT COUNT(*) FROM tareas) AS tareas;"

Write-Host ""
Write-Host "=== RESUMEN ==="
Get-ChildItem "evidencias\endpoints\*.txt" | ForEach-Object {
    $l = Get-Content $_.FullName | Where-Object { $_ -match "^(ID|Codigo HTTP obtenido|Resultado):" }
    ($l -join "  |  ")
}
