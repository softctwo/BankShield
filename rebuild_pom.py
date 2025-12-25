#!/usr/bin/env python3

import re

# Read the POM file
with open('/Users/zhangyanlong/workspaces/BankShield/bankshield-ai/pom.xml', 'r') as f:
    content = f.read()

# Remove any malformed repositories section that appears after </project>
if content.count('</project>') > 1:
    pos = content.rfind('</project>')
    content = content[:pos + 10]  # Keep only the first </project>

# Add repositories before the final </project>
if '<repositories>' not in content:
    new_content = content.replace(
        '</project>',
        '''    <!-- XGBoost official repository -->
    <repositories>
        <repository>
            <id>XGBoost4J Release Repo</id>
            <name>XGBoost4J Release Repo</name>
            <url>https://s3-us-west-2.amazonaws.com/xgboost-maven-repo/release/</url>
        </repository>
    </repositories>

</project>'''
    )
    
    with open('/Users/zhangyanlong/workspaces/BankShield/bankshield-ai/pom.xml', 'w') as f:
        f.write(new_content)
    
    print("✅ Fixed pom.xml structure")
else:
    print("Repositories already present")
