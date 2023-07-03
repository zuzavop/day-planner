<?php

function safe_get(array $params, string $name, $regexCheck = null, $default = null)
{
	if (!array_key_exists($name, $params)) return $default;
	if ($regexCheck && !preg_match($regexCheck, $params[$name])) return $default;
	return $params[$name];
}

$config = require __DIR__.'/db_config.php';
$connection = new mysqli($config['server'], $config['login'], $config['password'], $config['database']);
if ($connection->connect_error) {
    die("Could not connect to the database");
}

$name = safe_get($_GET, 'name');
$year = safe_get($_GET, 'year');
$month = safe_get($_GET, 'month');
$day = safe_get($_GET, 'day');
$time = safe_get($_GET, 'time');
$id = safe_get($_GET, 'id', '/^[0-9]+$/');

$sql = 'UPDATE calendar SET name="'
    . mysqli_real_escape_string($connection, $name) . '", year="'
    . mysqli_real_escape_string($connection, $year) . '", month="'
    . mysqli_real_escape_string($connection, $month) . '", day="'
    . mysqli_real_escape_string($connection, $day) . '", time='
    . ($time === '' ? 'NULL' : ('"' .  mysqli_real_escape_string($connection, $time) . '"')) . ' WHERE id = "'
    . mysqli_real_escape_string($connection, $id) . '"';

if ($connection->query($sql) === TRUE) {
    echo "Done";
} else {
    echo "Error updating record: " . $connection->error;
}

$connection->close();