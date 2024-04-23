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
    $username = $_GET['username'];
    $password = $_GET['password'];

    // Consulta SQL para verificar si el usuario ya existe
    $sql = "SELECT * FROM usuarios WHERE usuario='$username'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        // Usuario ya registrado
        echo "Usuario ya registrado";
    } else {
        // Inserta el nuevo usuario en la base de datos
        $sql = "INSERT INTO usuarios (usuario, contraseña) VALUES ('$username', '$password')";
        if ($conn->query($sql) === TRUE) {
            echo "Registro exitoso";
        } else {
            echo "Error: " . $sql . "<br>" . $conn->error;
        }
    }
} else {
    echo "Parámetros incorrectos";
}

// Cierra la conexión
$conn->close();
?>
