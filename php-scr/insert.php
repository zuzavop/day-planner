<?php

function json_response($error = '')
{
	header('Content-Type: application/json');
	$res = [ 'ok' => !$error ];
	if ($error) $res['error'] = $error;
	echo json_encode($res);
	exit;
}

function handle_error($connection) {
  json_response("Query error: " . $connection->error);
}

function set_new($new_id, $name, $year, $month, $day, $time) {
  $config = include __DIR__ . '/db_config.php';
  $connection = new mysqli($config['server'], $config['login'], $config['password'], $config['database']);
  if ($connection->connect_error) {
    json_response("Could not connect to the database");
  }
  $stmt_string = "INSERT INTO calendar (id, name, year, month, day, time) VALUES ('" 
        . mysqli_real_escape_string($connection, $new_id) . "', '" 
        . mysqli_real_escape_string($connection, $name) . "', '" 
        . mysqli_real_escape_string($connection, $year) . "', '" 
        . mysqli_real_escape_string($connection, $month) . "', '" 
        . mysqli_real_escape_string($connection, $day) . "', " 
        . ($time === 'NULL' ? $time : ("'" .  mysqli_real_escape_string($connection, $time) . "'")) . ")";
  $stmt = $connection->prepare($stmt_string) or handle_error($connection);
  $stmt->execute() or handle_error($connection);

  $connection->close();

  json_response();
}

function safe_get(array $params, string $name, $default = null, $regexCheck = null)
{
	if (!array_key_exists($name, $params)) return $default;
	if ($regexCheck && !preg_match($regexCheck, $params[$name])) return $default;
	return $params[$name];
}

function run() {
  $name = safe_get($_GET, 'name', '', '/^[0-9A-Za-z ]+$/');
  $new_id = safe_get($_GET, 'id', '', '/^[0-9]+$/');
  $year = safe_get($_GET, 'year', '', '/^[0-9]+$/');
  $month = safe_get($_GET, 'month', '', '/^[0-9]+$/');
  $day = safe_get($_GET, 'day', '', '/^[0-9]+$/');
  $time = safe_get($_GET, 'time', '', '/(^[0-9]+:[0-9]+$)/');

  if($name === '' || $new_id === '' || $year === '') {
    json_response("Id, name or time was invalid.");
  }

  if($time === ''){
    $time = 'NULL';
  } else {
    $time .= ':00';
  }

  set_new($new_id, $name, $year, $month, $day, $time);
}


try {
	run();
}
catch (Exception $e) {
	http_response_code(500);
	header('Content-Type: text/plain');
	echo $e->getMessage();
}