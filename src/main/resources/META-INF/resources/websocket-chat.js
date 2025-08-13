// const username = prompt("Enter your username:");
const wsProtocol = window.location.protocol === "https:" ? "wss" : "ws";
const wsHost = window.location.hostname || 'localhost';
const wsPort = window.location.port ? `:${window.location.port}` : '';
const ws = new WebSocket(`${wsProtocol}://${wsHost}${wsPort}/chat/user`);

const chatBox = document.getElementById("chat-box");
const userInput = document.getElementById("user-input");
const sendBtn = document.getElementById("send-btn");
const chatContainer = document.getElementById("chat-container");
const chatToggle = document.getElementById("chat-toggle");
const fullscreenToggle = document.getElementById("fullscreen-toggle");

chatToggle.addEventListener("click", () => {
    chatContainer.style.display = chatContainer.style.display === "none" ? "block" : "none";
});

fullscreenToggle.addEventListener("click", () => {
    chatContainer.classList.toggle("full-screen");
});

ws.onmessage = (event) => {
    appendMessage(event.data, "bot");
};

sendBtn.addEventListener("click", () => {
    sendMessage();
});

userInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        sendMessage();
    }
});

function sendMessage() {
    const message = userInput.value.trim();
    if (message) {
        appendMessage(message, "user");
        ws.send(message);
        userInput.value = "";
    }
}

function appendMessage(message, sender) {
    const messageElement = document.createElement("div");
    messageElement.classList.add("message", sender);

    // Render Markdown for bot responses
    if (sender === "bot") {
        let processedMessage = message;
        let originalYAML = extractYAMLFromMessage(message);
        let hasYAML = originalYAML !== null;
        
        // If the message doesn't already contain markdown code blocks, add them
        if (hasYAML && !message.includes('```yaml') && !message.includes('```yml')) {
            processedMessage = "```yaml\n" + originalYAML + "\n```";
        }
        
        // Configure marked options for better YAML rendering
        marked.setOptions({
            highlight: function(code, lang) {
                if (lang === 'yaml' || lang === 'yml') {
                    try {
                        return hljs.highlight(code, {language: 'yaml'}).value;
                    } catch (e) {
                        return code;
                    }
                }
                return code;
            },
            langPrefix: 'hljs language-'
        });
        
        messageElement.innerHTML = marked.parse(processedMessage);
        
        // Apply syntax highlighting to all code blocks
        messageElement.querySelectorAll('pre code').forEach((block) => {
            hljs.highlightElement(block);
        });
        
        // Add copy button for YAML content or any code blocks
        if (hasYAML || messageElement.querySelector('pre code')) {
            addCopyButton(messageElement, originalYAML || message);
        }
    } else {
        messageElement.textContent = message;
    }

    chatBox.appendChild(messageElement);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function extractYAMLFromMessage(message) {
    // Check if the message contains markdown code blocks
    const yamlBlockMatch = message.match(/```(?:yaml|yml)\n([\s\S]*?)\n```/);
    if (yamlBlockMatch) {
        return yamlBlockMatch[1].trim();
    }
    
    // If no markdown blocks, check if it's raw YAML
    if (isYAMLContent(message)) {
        return message;
    }
    
    return null;
}

function addCopyButton(messageElement, yamlContent) {
    const copyButton = document.createElement("button");
    copyButton.innerHTML = "📋 Copy YAML";
    copyButton.className = "copy-yaml-btn";
    copyButton.title = "Copy YAML to clipboard";
    
    copyButton.addEventListener("click", async () => {
        try {
            // Copy the raw YAML content without markdown formatting
            await navigator.clipboard.writeText(yamlContent);
            copyButton.innerHTML = "✅ Copied!";
            copyButton.title = "Copied!";
            
            // Reset button after 2 seconds
            setTimeout(() => {
                copyButton.innerHTML = "📋 Copy YAML";
                copyButton.title = "Copy YAML to clipboard";
            }, 2000);
        } catch (err) {
            console.error('Failed to copy: ', err);
            copyButton.innerHTML = "❌ Failed";
            copyButton.title = "Failed to copy";
            
            // Reset button after 2 seconds
            setTimeout(() => {
                copyButton.innerHTML = "📋 Copy YAML";
                copyButton.title = "Copy YAML to clipboard";
            }, 2000);
        }
    });
    
    // Add the copy button to the message element
    messageElement.appendChild(copyButton);
}

function isYAMLContent(text) {
    // Check if the text looks like YAML content
    const trimmedText = text.trim();
    
    // Check for common YAML patterns
    const yamlPatterns = [
        /^- ruleID:/m,           // Starts with rule definition
        /^[a-zA-Z_][a-zA-Z0-9_]*:/m,  // Starts with a key followed by colon
        /^\s*[a-zA-Z_][a-zA-Z0-9_]*:\s*$/m,  // Key with colon on its own line
        /^\s*- /m,               // List items
        /^\s*[a-zA-Z_][a-zA-Z0-9_]*:\s*[^\n]+$/m  // Key-value pairs
    ];
    
    // If it contains markdown code blocks, it's not raw YAML
    if (trimmedText.includes('```')) {
        return false;
    }
    
    // Check if it matches YAML patterns
    const isYAML = yamlPatterns.some(pattern => pattern.test(trimmedText));
    
    // Debug logging
    console.log('YAML detection:', { text: trimmedText.substring(0, 100), isYAML });
    
    return isYAML;
}
