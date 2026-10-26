\# NetXMS Device Simulator



A Java application that simulates network devices (such as switches) and pushes

realistic CPU, memory and temperature metrics into \[NetXMS](https://www.netxms.org/),

an open-source network monitoring system.



You can define \*\*scenarios\*\* such as overheating, CPU spikes or memory leaks, which

makes it possible to test thresholds and alarms without any real hardware.



\## Why



Monitoring systems are hard to test without real equipment. With only one or two

local machines there is not much data to work with, and you cannot control when

something goes wrong. This simulator provides:



\- Many devices from a single program

\- Values that look like real data (smooth waves with noise)

\- Full control over when and where problems occur



\## How it works



```

Simulator (Java) ──push──▶ NetXMS server ──▶ stored metrics

&#x20;                                └──▶ thresholds ──▶ alarms

```



1\. Each simulated device generates CPU, memory and temperature values.

2\. The simulator pushes the values to NetXMS using the official Java library

&#x20;  `netxms-client`.

3\. NetXMS stores the values and checks them against configured thresholds.

4\. When a value crosses a threshold, NetXMS raises an alarm.



The simulator uses \*\*push\*\* instead of polling. It sends the data on its own

schedule, so NetXMS does not need to reach the devices.



\## Tech stack



\- Java 17

\- Maven

\- `netxms-client` (NetXMS Java API)

\- NetXMS 6.2 with PostgreSQL



\## Project structure



| File | Purpose |

|---|---|

| `Simulator.java` | Entry point. Creates the devices, runs the main loop and applies scenarios. |

| `Device.java` | A simulated device. Generates values from a sine wave plus random noise. |

| `Metrics.java` | Metric names. These must match the DCI metric names in NetXMS. |

| `Scenario.java` | A timed event that forces a value, used to trigger alarms. |

| `DataSender.java` | Interface for anything that receives metric values. |

| `NetxmsSender.java` | Sends values to NetXMS through `netxms-client`. |

| `ConsoleSender.java` | Dry-run mode that only prints the values. |



\## Getting started



\### 1. Run without NetXMS



```

\--dry-run

```



The simulator prints values to the console every 5 seconds. This is a quick way

to check that everything builds and runs.



\### 2. Prepare NetXMS



1\. \*\*Create nodes\*\* named `switch1`, `switch2` and so on. The names must match

&#x20;  exactly. The nodes do not need an IP address, but disable agent, SNMP and ICMP

&#x20;  polling, since data arrives via push.

2\. \*\*Create a template\*\* with three DCIs, all with origin \*\*Push\*\*:

&#x20;  - `cpu.usage` (%)

&#x20;  - `memory.usage` (%)

&#x20;  - `temperature` (°C)



&#x20;  Enter the name in the \*\*Metric\*\* field. Filling in only the display name is

&#x20;  not enough.

3\. \*\*Add thresholds\*\*, for example CPU > 90, memory > 90 and temperature > 80.

4\. \*\*Apply the template\*\* to the nodes.

5\. \*\*Permissions:\*\* give the user the simulator logs in as the "Push data"

&#x20;  access right on the nodes.



\### 3. Run against NetXMS



Store the password in an environment variable so it never ends up in the code:



```

NETXMS\_PASSWORD=your-password

```



Program arguments:



```

\--server 127.0.0.1 --user simulator --devices 3 --interval 5

```



| Flag | Default | Description |

|---|---|---|

| `--server` | 127.0.0.1 | NetXMS server address |

| `--port` | 4701 | NetXMS client port |

| `--user` | admin | NetXMS user |

| `--devices` | 5 | Number of simulated devices |

| `--interval` | 5 | Seconds between pushes |

| `--dry-run` | off | Print values without sending them |



The `netxms-client` version in `pom.xml` must match your NetXMS server version.



\## Scenarios



A scenario forces a value on one device for a period of time. Scenarios are

defined in `Simulator.java` and repeat every 6 minutes.



```java

// device, metric, start (s), end (s), from, to

new Scenario("switch1", Metrics.TEMPERATURE, 30, 90, 85, 85)

```



The value moves linearly from `from` to `to` during the period. Use the same

value for both to get a constant value.



Built-in scenarios:



| Time | Device | Event |

|---|---|---|

| 60–120 s | switch2 | CPU spike to 95 % |

| 120–300 s | switch3 | Memory leak, 60 % → 98 % |

| 200–260 s | switch1 | Overheating at 85 °C |



\## Design decisions



\- \*\*Push instead of polling.\*\* The simulator controls the timing, and no network

&#x20; access to the devices is needed.

\- \*\*`DataSender` interface.\*\* The same loop can send to NetXMS or to the console,

&#x20; which makes it easy to test without a server.

\- \*\*Data goes through NetXMS, not directly into a database.\*\* This way the whole

&#x20; pipeline is tested, including thresholds and alarms.

\- \*\*No secrets in code.\*\* Passwords are read from environment variables.



\## Troubleshooting



| Problem | Likely cause |

|---|---|

| Login fails | Wrong address, port, user or `NETXMS\_PASSWORD` |

| `Invalid DCI ID` | The DCI has no metric name (only the display name is set) |

| Values are not sent | Node or DCI missing, misspelled name, DCI not of type Push, or the user lacks the "Push data" right |

| No alarm | Missing threshold, or the scenario targets a node that does not exist |



\## Background



Built as part of a third-year project course in Computer Science and Engineering

at Lund University.

