  const form = document.getElementById("form");
const canvas = document.getElementById("canvas");
const ctx = canvas.getContext("2d");
const message = document.getElementById("message");
const resultsBody = document.getElementById("results");
const storageKey = "point-results";

let results = [];
try {
  const saved = JSON.parse(localStorage.getItem(storageKey) || "[]");
  if (Array.isArray(saved)) results = saved;
} catch (error) {
  results = [];
}

// Например, "2.001" храним точно как 2001 / 1000.
function parseDecimal(text) {
  const negative = text.startsWith("-");
  const unsigned = text.replace(/^[+-]/, "");
  const [integer, fraction = ""] = unsigned.split(".");
  const scale = 10n ** BigInt(fraction.length);
  const digits = BigInt((integer || "0") + fraction);
  return { value: negative ? -digits : digits, scale };
}

function isInside(xText, y, r) {
  const decimal = parseDecimal(xText);
  const x = 2n * decimal.value;
  const exactY = BigInt(y) * 2n * decimal.scale;
  const exactR = BigInt(r * 2) * decimal.scale;

  const rectangle = x >= -exactR && x <= 0n && exactY >= 0n && 2n * exactY <= exactR;
  const circle = x <= 0n && exactY <= 0n && x * x + exactY * exactY <= exactR * exactR;
  const triangle = x >= 0n && x <= exactR && exactY <= 0n && 2n * exactY >= x - exactR;
  return rectangle || circle || triangle;
}

function showResults() {
  resultsBody.innerHTML = "";

  for (const item of results) {
    const row = resultsBody.insertRow();
    row.insertCell().textContent = item.x;
    row.insertCell().textContent = item.y;
    row.insertCell().textContent = item.r;
    row.insertCell().textContent = item.inside ? "Да" : "Нет";
    row.insertCell().textContent = new Date(item.time).toLocaleString("ru-RU", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    });
  }
}

function drawArea(r, x, y, inside) {
  const center = 200;
  const scale = 35; 
  const radius = r * scale;

  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#3399ff";

  // Прямоугольник: слева сверху.
  ctx.fillRect(center - radius, center - radius / 2, radius, radius / 2);

  // Четверть круга: слева снизу.
  ctx.beginPath();
  ctx.moveTo(center, center);
  ctx.arc(center, center, radius, Math.PI / 2, Math.PI);
  ctx.closePath();
  ctx.fill();

  // Треугольник: справа снизу.
  ctx.beginPath();
  ctx.moveTo(center, center);
  ctx.lineTo(center + radius, center);
  ctx.lineTo(center, center + radius / 2);
  ctx.closePath();
  ctx.fill();

  // Оси и стрелки.
  ctx.strokeStyle = "black";
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(15, center);
  ctx.lineTo(385, center);
  ctx.moveTo(385, center);
  ctx.lineTo(379, center - 4);
  ctx.moveTo(385, center);
  ctx.lineTo(379, center + 4);
  ctx.moveTo(center, 385);
  ctx.lineTo(center, 15);
  ctx.moveTo(center, 15);
  ctx.lineTo(center - 4, 21);
  ctx.moveTo(center, 15);
  ctx.lineTo(center + 4, 21);
  ctx.stroke();

  ctx.font = "13px Arial";
  ctx.fillStyle = "black";
  ctx.fillText("x", 385, center - 8);
  ctx.fillText("y", center + 7, 17);

  const marks = [
    [-r, "-R"],
    [-r / 2, "-R/2"],
    [r / 2, "R/2"],
    [r, "R"]
  ];

  for (const [value, label] of marks) {
    const xMark = center + value * scale;
    const yMark = center - value * scale; 

    ctx.beginPath();
    ctx.moveTo(xMark, center - 4);
    ctx.lineTo(xMark, center + 4);
    ctx.moveTo(center - 4, yMark);
    ctx.lineTo(center + 4, yMark);
    ctx.stroke();

    ctx.fillText(label, xMark - 12, center - 8);
    ctx.fillText(label, center + 6, yMark + 4);
  }

  // Точка появляется после проверки формы.
  if (x !== undefined && y !== undefined) {
    ctx.fillStyle = inside ? "green" : "red";
    ctx.beginPath();
    ctx.arc(center + x * scale, center - y * scale, 4, 0, 2 * Math.PI);
    ctx.fill();
  }
}

document.querySelectorAll('input[name="r"]').forEach((radio) => {
  radio.addEventListener("change", () => drawArea(Number(radio.value)));
});

form.addEventListener("submit", (event) => {
  event.preventDefault();

  const xText = document.getElementById("x").value.trim().replace(",", ".");
  const yText = document.getElementById("y").value;
  const chosenR = document.querySelector('input[name="r"]:checked');

  if (!/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)$/.test(xText)) {
    message.textContent = "Введите число X.";
    return;
  }

  const decimalX = parseDecimal(xText);
  if (decimalX.value < -5n * decimalX.scale || decimalX.value > 3n * decimalX.scale) {
    message.textContent = "X должен быть от −5 до 3.";
    return;
  }
  // Для рисунка достаточно Number; проверку выполняем по исходной строке.
  const x = Number(xText);

  const y = Number(yText);
  if (yText === "" || !Number.isInteger(y) || y < -5 || y > 3) {
    message.textContent = "Выберите Y.";
    return;
  }

  if (!chosenR || !["1", "1.5", "2", "2.5", "3"].includes(chosenR.value)) {
    message.textContent = "Выберите R.";
    return;
  }

  const r = Number(chosenR.value);
  const inside = isInside(xText, y, r);
  results.push({ x: xTex , y, r, inside, time: Date.now() });

  try {
    localStorage.setItem(storageKey, JSON.stringify(results));
    message.textContent = inside ? "Точка попала в область." : "Точка не попала в область.";
  } catch (error) {
    message.textContent = "Результат добавлен, но сохранить его не удалось.";
  }

  showResults();
  drawArea(r, x, y, inside);
});

showResults();
drawArea(3);

// Повторный вывод учитывает текущий часовой пояс устройства.
setInterval(showResults, 60000);
