import re
import os
import glob

modules = [
    'bar-service',
    'dropPoint-service',
    'eventPlanner-service',
    'users-service',
    'warehouse-service',
    'servers/config-server',
    'servers/eureka-server',
    'servers/gateway'
]

base_dir = r"d:\FH-Dortmund\SecondSemester\SoftwareIntensiveSolution\Project\nextBar"

for mod in modules:
    pom_path = os.path.join(base_dir, mod, 'pom.xml')
    if not os.path.exists(pom_path):
        print(f"File not found: {pom_path}")
        continue
    
    with open(pom_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Replace the entire <parent> block with the local parent configuration
    parent_pattern = re.compile(r'<parent>.*?</parent>', re.DOTALL)
    
    # Calculate relativePath: if contains '/', it's two levels down (servers/xxx)
    if '/' in mod or '\\' in mod:
        rel_path = "../../pom.xml"
    else:
        rel_path = "../pom.xml"
        
    new_parent = f"""<parent>
		<groupId>com.nextbar</groupId>
		<artifactId>nextbar-bom</artifactId>
		<version>1.0.0-SNAPSHOT</version>
		<relativePath>{rel_path}</relativePath>
	</parent>"""
    
    # Check if there is already a parent block
    if parent_pattern.search(content):
        content = parent_pattern.sub(new_parent, content)
    else:
        # Insert it right below <modelVersion>
        content = re.sub(r'(<modelVersion>.*?</modelVersion>)', r'\1\n\t' + new_parent, content)

    # 2. Remove the entire <dependencyManagement> block
    dep_management_pattern = re.compile(r'<dependencyManagement>.*?</dependencyManagement>', re.DOTALL)
    content = dep_management_pattern.sub('', content)

    # 3. Remove redundant properties (spring-cloud.version, java.version, spring-boot.version)
    content = re.sub(r'\s*<spring-cloud\.version>.*?</spring-cloud\.version>', '', content)
    content = re.sub(r'\s*<java\.version>.*?</java\.version>', '', content)
    content = re.sub(r'\s*<spring-boot\.version>.*?</spring-boot\.version>', '', content)
    
    # Remove any empty <properties> block resulting from above removal
    content = re.sub(r'<properties>\s*</properties>', '', content)

    with open(pom_path, 'w', encoding='utf-8') as f:
        f.write(content)
        
    print(f"Updated {mod}/pom.xml")
