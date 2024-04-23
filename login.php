<?php
// Establece la conexión con la base de datos
$servername = "localhost:3307";
$username = "root";
$password = "gonzalo123";
$dbname = "movie_database";

$conn = new mysqli($servername, $username, $password, $dbname);

// Verifica la conexión
if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

// Verifica si se han recibido los parámetros de usuario y contraseña
if (isset($_GET['username']) && isset($_GET['password'])) {
    $username = $conn->real_escape_string($_GET['username']);
    $password = $conn->real_escape_string($_GET['password']);

    // Consulta SQL utilizando una consulta preparada para evitar la inyección SQL
    $sql = "SELECT * FROM usuarios WHERE usuario=? AND contraseña=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $username, $password);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // Inicio de sesión exitoso
        echo json_encode(array("status" => "success", "message" => "Login exitoso"));
    } else {
        // Inicio de sesión fallido
        echo json_encode(array("status" => "failure", "message" => "Credenciales incorrectas"));
    }
} else {
    echo json_encode(array("status" => "failure", "message" => "Parámetros incorrectos"));
}

// Cierra la conexión
$conn->close();
?>
