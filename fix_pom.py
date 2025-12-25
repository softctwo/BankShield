#!/usr/bin/env python3

# Fix pom.xml XGBoost version

import re

with open('/Users/zhangyanlong/workspaces/BankShield/bankshield-ai/pom.xml', 'r') as f:
    content = f.read()

# Replace XGBoost version
print("Original XGBoost version:")
match = re.search(r'xgboost4j.*?<version>([^<]+)</version>', content, re.DOTALL)
if match:
    old_version = match.group(1)
    print(f"  Old: {old_version}")
    
    # Replace
    new_content = content.replace(
        '<version>1.6.1</version>  <!-- XGBoost -->',
        '<version>1.7.3</version>  <!-- XGBoost -->'
    )
    
    # If simple replace didn't work, try more direct
    new_content = re.sub(
        r'(<artifactId>xgboost4j</artifactId>\s*<version>)1\.6\.1(</version>)',
        r'\g<1>1.7.3\g<2>',
        new_content
    )
    
    with open('/Users/zhangyanlong/workspaces/BankShield/bankshield-ai/pom.xml', 'w') as f:
        f.write(new_content)
    
    print("✅ Fixed!")
    
    # Verify
    with open('/Users/zhangyanlong/workspaces/BankShield/bankshield-ai/pom.xml', 'r') as f:
        verify = f.read()
    
    match = re.search(r'xgboost4j.*?<version>([^<]+)</version>', verify, re.DOTALL)
    if match:
        print(f"  New: {match.group(1)}")
else:
    print("❌ Could not find XGBoost version")
