#!/usr/bin/env python3

import json

functions = {
    "spring-warm": ("Spring Cloud Function", "#29660C"),
    "spring": ("Spring Cloud Function", "#29660C"),
    "spring-snap": ("Spring Cloud Function (SnapStart)", "#81B622"),
    "micronaut": ("Micronaut", "#578E87"),
    "micronaut-snap": ("Micronaut (SnapStart)", "#189AB4"),
    "micronaut-native": ("Micronaut (Native Image)", "#75E6DA"),
    "rust": ("Rust", "#B04410"),
}

graphs = {
    "spring-warm": ("spring-warm",),
    "spring": ("spring",),
    "micronaut": (
        "spring",
        "micronaut",
    ),
    "snap": (
        "spring",
        "micronaut",
        "spring-snap",
        "micronaut-snap",
    ),
    "rust": (
        "spring",
        "micronaut",
        "spring-snap",
        "micronaut-snap",
        "rust",
    ),
    "micronaut-native": (
        "spring",
        "micronaut",
        "spring-snap",
        "micronaut-snap",
        "rust",
        "micronaut-native",
    ),
}

datasets = {}

for designator, (label, color) in functions.items():
    with open(designator) as f:
        measurements = [float(line.strip()) for line in f.readlines()]
    dataset = {
        "label": label,
        "backgroundColor": color,
        "data": [
            {
                "x": index,
                "y": time,
            }
            for (index, time) in enumerate(measurements)
        ],
    }
    datasets[designator] = dataset
print(datasets)

for graph, included in graphs.items():
    data = {"datasets": [datasets[dataset] for dataset in included]}
    with open(f"{graph}.json", "w") as f:
        json.dump(data, f)
