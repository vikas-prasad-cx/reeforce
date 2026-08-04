# Class-Section Forecast Adapter Guide

This guide describes how to convert student registration forecast exports (e.g., from Student Information Systems / SIS short-horizon forecasts) into a `DemandSeries` CSV format for Reeforce `--domain class-section`, and evaluate it against available campus capacity (Roster).

---

## 1. Required CSV Schemas

To run a full capacity and gap analysis, Reeforce requires two datasets: **Demand** (student seat requests) and **Roster** (available class section capacity).

### A. Demand CSV Schema (`demand.csv`)
Represents incoming student registration volume or forecast pressure over time.

| Column Name | Data Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `start_iso` | String (ISO-8601) | Start timestamp of the observation window. | `2026-08-18T14:00:00Z` |
| `end_iso` | String (ISO-8601) | End timestamp of the observation window. | `2026-08-18T14:15:00Z` |
| `offered_volume` | Integer | Total seat requests or registration volume. | `40` |
| `aht_seconds` | Integer | Class duration or interval length in seconds. | `900` |

### B. Roster CSV Schema (`roster.csv`)
Represents the available physical or scheduled section infrastructure on campus.

| Column Name | Data Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `agent_id` | String | Section identifier or overflow bucket name. | `SEC-A` |
| `start_iso` | String (ISO-8601) | Section availability start time. | `2026-08-18T14:00:00Z` |
| `end_iso` | String (ISO-8601) | Section availability end time. | `2026-08-18T15:30:00Z` |
| `state` | String | Availability status (`AVAILABLE` or `OFFLINE`). | `AVAILABLE` |
| `capacity` | Integer | Maximum seat capacity for this section. | `30` |

---

## 2. Example Datasets

### Forecast Demand (`demand.csv`)
start_iso,end_iso,offered_volume,aht_seconds
2026-08-18T14:00:00Z,2026-08-18T14:15:00Z,40,900
2026-08-18T14:15:00Z,2026-08-18T14:30:00Z,45,900
2026-08-18T14:30:00Z,2026-08-18T14:45:00Z,70,900
2026-08-18T14:45:00Z,2026-08-18T15:00:00Z,75,900

### Campus Roster (`roster.csv`)
agent_id,start_iso,end_iso,state,capacity
SEC-A,2026-08-18T14:00:00Z,2026-08-18T15:30:00Z,AVAILABLE,30
SEC-B,2026-08-18T14:00:00Z,2026-08-18T15:30:00Z,AVAILABLE,25
SEC-OVERFLOW,2026-08-18T14:00:00Z,2026-08-18T14:30:00Z,OFFLINE,25

---

## 3. Verification & Round-Trip Evaluation

To test and verify the demand series against a target class-section roster:

1. Place your generated `demand.csv` alongside `roster.csv` inside your dataset directory    
(e.g., `datasets/class-section-overfill/`).
2. Run the Reeforce capacity evaluation for `--domain class-section`:

```bash
reeforce evaluate --domain class-section --roster datasets/class-section-overfill/roster.csv --demand datasets/class-section-overfill/demand.csv