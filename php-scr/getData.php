<?php

function load_plans($name, $year, $month, $day) {
  $config = include __DIR__ . '/db_config.php';
  $connection = new mysqli($config['server'], $config['login'], $config['password'], $config['database']);
  if ($connection->connect_error) {
    json_response("Could not connect to the database");
  }

  $stmt_string = "SELECT * FROM calendar WHERE LOCATE('"
      . mysqli_real_escape_string($connection, $name) . "', name) > 0"
      . ($year === '' ? "" : " AND year=" . mysqli_real_escape_string($connection, $year) . "'")
      . ($month === '' ? "" : " AND month=" . mysqli_real_escape_string($connection, $month) . "'")
      . ($day === '' ? "" : " AND day=" . mysqli_real_escape_string($connection, $day) . "'");

  $stmt = $connection->prepare($stmt_string) or handle_error($connection);
  $stmt->execute() or handle_error($connection);
  $stmt_result = $stmt->get_result() or handle_error($connection);
  $result = null;
  $number = 0;
  while ($row = $stmt_result->fetch_assoc()) {
      $result[$number] = $row;
      $number += 1;
  }
  $connection->close();

  echo json_encode($result);
}

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

function safe_get(array $params, string $name, $default = null, $regexCheck = null)
{
	if (!array_key_exists($name, $params)) return $default;
	if ($regexCheck && !preg_match($regexCheck, $params[$name])) return $default;
	return $params[$name];
}

function run() {
  $name = safe_get($_GET, 'name', '');
  $year = safe_get($_GET, 'year', '', '/^[0-9]+$/');
  $month = safe_get($_GET, 'month', '', '/^[0-9]+$/');
  $day = safe_get($_GET, 'day', '', '/^[0-9]+$/');

  load_plans($name, $year, $month, $day);
}


try {
	run();
}
catch (Exception $e) {
	http_response_code(500);
	header('Content-Type: text/plain');
	echo $e->getMessage();
}