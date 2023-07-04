<?php

function safe_get(array $params, string $name, $default = null, $regexCheck = null)
{
	if (!array_key_exists($name, $params)) return $default;
	if ($regexCheck && !preg_match($regexCheck, $params[$name])) return $default;
	return $params[$name];
}

$new_id = safe_get($_GET, 'id', '', '/^[0-9]+$/');
if ($new_id === '') {
  http_response_code(500);
  exit;
}

$config = require __DIR__.'/db_config.php';
$connection = new mysqli($config['server'], $config['login'], $config['password'], $config['database']);
if ($connection->connect_error) {
  http_response_code(500);
  die("Could not connect to the database");
}

$sql = "DELETE FROM calendar WHERE id = '" . $connection->real_escape_string($new_id) . "'";

if ($connection->query($sql) === TRUE) {
  echo "Record deleted successfully";
} else {
  http_response_code(500);
  echo "Error deleting record: " . $connection->error;
}

$connection->close();